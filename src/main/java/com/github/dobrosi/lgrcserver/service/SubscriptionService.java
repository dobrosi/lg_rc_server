package com.github.dobrosi.lgrcserver.service;

import com.github.dobrosi.lgrcserver.model.Customer;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class SubscriptionService {

    private final CustomerService customerService;

    @Autowired
    public SubscriptionService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public void activatePremiumSubscription(String keycloakId, String stripeCustomerId) {
        Customer user = getOrCreateByKeycloakId(keycloakId);
        user.setStripeCustomerId(stripeCustomerId);
        user.setPremium(true);
        log.info("Prémium előfizetés aktiválva a Keycloak ID-hoz: {}", keycloakId);
    }

    public Customer getOrCreateByKeycloakId(final String keycloakId) {
        return customerService.findOrCreateByKeycloakId(keycloakId);
    }

    public void deactivatePremiumSubscription(final String stripeCustomerId) {
        customerService.findByStripeCustomerId(stripeCustomerId).setPremium(false);
    }
}
