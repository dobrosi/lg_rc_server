package com.github.dobrosi.lgrcserver.service.stripe;

import com.github.dobrosi.lgrcserver.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService {

    private final SubscriptionService subscriptionService;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${callback.url}")
    private String callbackUrl;

    @Value("${stripe.api.secretKey}")
    private String secretKey;

    @Value("${stripe.api.webhookSigningSecret}")
    private String webhookSigningSecret;

    @Value("${stripe.price.premiumId}")
    private String premiumPriceId;

    public StripeService(final SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public String createCheckoutSession(String keycloakUserId, String email) throws StripeException {
        log.info("Stripe checkout session létrehozása Keycloak ID-hoz: {}", keycloakUserId);
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl(serverUrl + "/public/stripe/webhook/success")
            .setCancelUrl(serverUrl + "/public/stripe/webhook/cancel")
            .setClientReferenceId(keycloakUserId)
            .setCustomerEmail(email)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(premiumPriceId)
                    .setQuantity(1L)
                    .build()
            )
            .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            log.error("Error creating Stripe checkout session", e);
            throw e;
        }
    }

    public void webhook(
        final String payload,
        final String sigHeader
    ) throws SignatureVerificationException, EventDataObjectDeserializationException {
        log.info("Stripe webhook érkezett. payload: {}, sigHeader: {}", payload, sigHeader);
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSigningSecret);

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        if (dataObjectDeserializer.getObject()
            .isPresent()) {
            stripeObject = dataObjectDeserializer.getObject()
                .get();
        } else {
            stripeObject = dataObjectDeserializer.deserializeUnsafe();
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) stripeObject;
            String customerId = session.getCustomer();
            String clientReferenceId = session.getClientReferenceId();
            subscriptionService.activatePremiumSubscription(
                clientReferenceId,
                customerId);
        } else if ("customer.subscription.deleted".equals(event.getType())) {
            Subscription subscription = (Subscription) stripeObject;
            String stripeCustomerId = subscription.getCustomer();
            log.info("Előfizetés lejárt! Stripe Customer ID: {}", stripeCustomerId);
            subscriptionService.deactivatePremiumSubscription(stripeCustomerId);
        } else {
            log.info("Kaptunk egy nem kezelt eseményt: {}", event.getType());
        }
    }

    public String createCustomerPortalUrl(
            String keycloakUserId,
            String queryParts) {
        try {
            String stripeCustomerId = subscriptionService.getOrCreateByKeycloakId(keycloakUserId).getStripeCustomerId();
            SessionCreateParams params = new SessionCreateParams.Builder()
                .setCustomer(stripeCustomerId)
                .setReturnUrl(callbackUrl + (Strings.isEmpty(queryParts) ? "" : "?" + queryParts))
                .build();
            Session session = Session.create(params);
            return session.getUrl();

        } catch (Exception e) {
            log.error("Error creating Stripe customer portal URL", e);
            throw new RuntimeException(e);
        }
    }
}
