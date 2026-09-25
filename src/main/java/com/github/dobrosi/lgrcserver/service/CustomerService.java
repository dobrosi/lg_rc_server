package com.github.dobrosi.lgrcserver.service;

import com.github.dobrosi.lgrcserver.dao.CustomerRepository;
import com.github.dobrosi.lgrcserver.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrCreateByKeycloakId(String keycloakId) {
        return customerRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> customerRepository.save(new Customer(keycloakId)));
    }

    public Customer findByStripeCustomerId(final String stripeCustomerId) {
        return customerRepository.findByStripeCustomerId(stripeCustomerId);
    }
}
