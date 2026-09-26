package com.github.dobrosi.lgrcserver.controller.stripe;

import java.net.URI;

import com.github.dobrosi.lgrcserver.service.stripe.StripeService;
import com.stripe.exception.SignatureVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/stripe")
@RequestMapping
@Slf4j
public class StripeController {
    @Value("${callback.url}")
    private String callbackUrl;

    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            stripeService.webhook(payload, sigHeader);
        } catch (SignatureVerificationException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing payload");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(callbackUrl + "?stripe-webhook")).build();
    }
}
