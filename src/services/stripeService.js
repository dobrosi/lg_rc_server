const stripe = require('stripe')(process.env.STRIPE_API_SECRET_KEY);
const subscriptionService = require('./subscriptionService');

class StripeService {
  constructor() {
    this.serverUrl = process.env.SERVER_URL;
    this.prices = {
      'premium-annual': {
        id: process.env.STRIPE_PRICE_PREMIUM_ANNUAL_ID,
        subscription: true
      },
      'premium-monthly': {
        id: process.env.STRIPE_PRICE_PREMIUM_MONTHLY_ID,
        subscription: true
      },
      'premium-lifetime': {
        id: process.env.STRIPE_PRICE_PREMIUM_LIFETIME_ID,
        subscription: false
      }
    };
  }

  async createCheckoutSession(keycloakUserId, email, planId) {
    console.log(`Stripe checkout session létrehozása Keycloak ID-hoz: ${keycloakUserId}`);
    
    let isTrial = planId.toLowerCase().endsWith('-trial');
    let basePlanId = planId.replace('-trial', '');
    
    const priceConfig = this.prices[basePlanId];
    if (!priceConfig) {
      throw new Error(`Ismeretlen planId: ${basePlanId}`);
    }

    const sessionParams = {
      mode: priceConfig.subscription ? 'subscription' : 'payment',
      success_url: `${this.serverUrl}/public/index.html?success=true`,
      cancel_url: `${this.serverUrl}/public/index.html?cancel=true`,
      client_reference_id: keycloakUserId,
      customer_email: email,
      line_items: [
        {
          price: priceConfig.id,
          quantity: 1,
        },
      ],
    };

    if (isTrial && priceConfig.subscription) {
      sessionParams.subscription_data = {
        trial_period_days: 7,
      };
    }

    const session = await stripe.checkout.sessions.create(sessionParams);
    return session.url;
  }

  async handleWebhook(payload, sigHeader) {
    let event;
    try {
      event = stripe.webhooks.constructEvent(
        payload,
        sigHeader,
        process.env.STRIPE_API_WEBHOOK_SIGNING_SECRET
      );
    } catch (err) {
      console.error(`Webhook aláírás ellenőrzés sikertelen: ${err.message}`);
      throw err;
    }

    const dataObject = event.data.object;

    if (event.type === 'checkout.session.completed') {
      const session = dataObject;
      const customerId = session.customer;
      const clientReferenceId = session.client_reference_id;
      await subscriptionService.activatePremiumSubscription(clientReferenceId, customerId);
    } else if (event.type === 'customer.subscription.deleted') {
      const subscription = dataObject;
      const stripeCustomerId = subscription.customer;
      console.log(`Előfizetés lejárt! Stripe Customer ID: ${stripeCustomerId}`);
      await subscriptionService.deactivatePremiumSubscription(stripeCustomerId);
    } else {
      console.log(`Nem kezelt esemény: ${event.type}`);
    }
  }

  async createCustomerPortalUrl(keycloakUserId, queryParts) {
    const customer = await subscriptionService.getOrCreateByKeycloakId(keycloakUserId);
    const stripeCustomerId = customer.stripeCustomerId;

    if (!stripeCustomerId) {
      throw new Error("Nincs Stripe Customer ID ehhez a felhasználóhoz.");
    }

    const session = await stripe.billingPortal.sessions.create({
      customer: stripeCustomerId,
      return_url: `${this.serverUrl}${queryParts ? '?' + queryParts : ''}`,
    });

    return session.url;
  }
}

module.exports = new StripeService();
