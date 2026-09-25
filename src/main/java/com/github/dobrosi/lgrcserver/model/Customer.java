package com.github.dobrosi.lgrcserver.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Customer {
    @Id
    @Column(unique = true, nullable = false)
    private String keycloakId;

    @Column(unique = true)
    private String stripeCustomerId;

    @Column(nullable = false)
    private boolean isPremium = false;

    @OneToMany
    private List<Device> devices;

    public Customer(String keycloakId) {
        this.keycloakId = keycloakId;
    }
}
