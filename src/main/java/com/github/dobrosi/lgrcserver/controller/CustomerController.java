package com.github.dobrosi.lgrcserver.controller;

import java.net.URI;

import com.github.dobrosi.lgrcserver.dto.CheckoutDto;
import com.github.dobrosi.lgrcserver.dto.CustomerDto;
import com.github.dobrosi.lgrcserver.service.CustomerService;
import com.github.dobrosi.lgrcserver.service.stripe.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    private final CustomerService customerService;

    private final StripeService stripeService;

    @Autowired
    public CustomerController(CustomerService customerService, StripeService stripeService) {
        this.customerService = customerService;
        this.stripeService = stripeService;
    }

    @GetMapping
    public ResponseEntity<CustomerDto> getCustomer(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(
            new CustomerDto(
                customerService.findOrCreateByKeycloakId(jwt.getSubject()).isPremium()),
            HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutDto> createCheckoutSession(@AuthenticationPrincipal Jwt jwt) {
        // A Keycloak tokenből a 'sub' claim tartalmazza a felhasználó egyedi azonosítóját
        String keycloakUserId = jwt.getSubject();

        try {
            String checkoutUrl = stripeService.createCheckoutSession(keycloakUserId, jwt.getClaimAsString("email"));
            return ResponseEntity.ok(new CheckoutDto(checkoutUrl));

        } catch (StripeException e) {
            // Ha a Stripe szerver hibát dob (pl. rossz API kulcs)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Stripe hiba: " + e.getMessage());
        }
    }

    @PostMapping("/customer-portal")
    public ResponseEntity<Void> createPortalSession(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String queryParts) {
        return ResponseEntity.status(HttpStatus.FOUND).location(
            URI.create(stripeService.createCustomerPortalUrl(jwt.getSubject(), queryParts))).build();
    }
}
