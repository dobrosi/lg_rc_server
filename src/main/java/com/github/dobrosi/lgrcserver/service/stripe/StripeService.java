package com.github.dobrosi.lgrcserver.service.stripe;

import com.github.dobrosi.lgrcserver.config.StripeProperties;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import static com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT;
import static com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeService {

    private final SubscriptionService subscriptionService;

    private final StripeProperties stripeProperties;

    private final ObjectMapper objectMapper;

    @Value("${server.url}")
    private String serverUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getApi().getSecretKey();
    }

    public String createCheckoutSession(String keycloakUserId, String email, String planId) throws StripeException {
        log.info("Stripe checkout session létrehozása Keycloak ID-hoz: {}", keycloakUserId);
        StripeProperties.Price price = stripeProperties.getPrices()
            .get(planId);
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(price.isSubscription() ? SUBSCRIPTION : PAYMENT)
            .setSuccessUrl(serverUrl + "/public/index.html?success=true")
            .setCancelUrl(serverUrl + "/public/index.html?cancel=true")
            .setClientReferenceId(keycloakUserId)
            .setCustomerEmail(email)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(price.getId())
                    .setQuantity(1L)
                    .build()
            )
            .build();

        try {
            Session session = Session.create(params);
            log.info("Stripe checkout session created: {}", objectMapper.writeValueAsString(
                new String[]{session.getClientReferenceId(), session.getUrl(), session.getId(), session.getCustomer(), session.getSubscription(), session.getMode()}));
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
        Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getApi().getWebhookSigningSecret());

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
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
                .setReturnUrl(serverUrl + (Strings.isEmpty(queryParts) ? "" : "?" + queryParts))
                .build();
            Session session = Session.create(params);
            return session.getUrl();

        } catch (Exception e) {
            log.error("Error creating Stripe customer portal URL", e);
            throw new RuntimeException(e);
        }
    }
}
