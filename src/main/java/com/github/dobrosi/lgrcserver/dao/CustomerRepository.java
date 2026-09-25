package com.github.dobrosi.lgrcserver.dao;

import java.util.Optional;

import com.github.dobrosi.lgrcserver.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByKeycloakId(String keycloakId);

    Customer findByStripeCustomerId(String stripeCustomerId);
}
