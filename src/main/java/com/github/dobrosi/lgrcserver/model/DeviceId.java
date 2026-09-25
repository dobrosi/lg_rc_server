package com.github.dobrosi.lgrcserver.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class DeviceId {
    @ManyToOne
    private Customer customer;

    private String ip;

    private String clientKey;
}
