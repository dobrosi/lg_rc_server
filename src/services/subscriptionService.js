const { db, customers } = require('../models/db');
const { eq } = require('drizzle-orm');
const customerService = require('./customerService');

class SubscriptionService {
  async activatePremiumSubscription(keycloakId, stripeCustomerId) {
    await customerService.findOrCreateByKeycloakId(keycloakId);
    await db.update(customers)
      .set({ stripeCustomerId, isPremium: true })
      .where(eq(customers.keycloakId, keycloakId));
    console.log(`Prémium előfizetés aktiválva a Keycloak ID-hoz: ${keycloakId}`);
  }

  async getOrCreateByKeycloakId(keycloakId) {
    return await customerService.findOrCreateByKeycloakId(keycloakId);
  }

  async deactivatePremiumSubscription(stripeCustomerId) {
    const customer = await customerService.findByStripeCustomerId(stripeCustomerId);
    if (customer) {
      await db.update(customers)
        .set({ isPremium: false })
        .where(eq(customers.stripeCustomerId, stripeCustomerId));
      console.log(`Előfizetés leállítva Stripe Customer ID-hoz: ${stripeCustomerId}`);
    }
  }
}

module.exports = new SubscriptionService();
