package com.github.dobrosi.lgrcserver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Device {
    @Id
    private DeviceId deviceId;

    private String name;

    private String model;

    public Device(final DeviceId deviceId) {
        this.deviceId = deviceId;
    }
}
