package com.github.dobrosi.lgrcserver.service;

import java.util.Collection;
import java.util.List;

import com.github.dobrosi.lgrcserver.dto.DeviceDto;
import com.github.dobrosi.lgrcserver.model.Customer;
import com.github.dobrosi.lgrcserver.model.Device;
import com.github.dobrosi.lgrcserver.model.DeviceId;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DeviceService {

    private final CustomerService customerService;

    public DeviceService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public List<DeviceDto> getDevices(String keycloakId) {
        return getCustomer(keycloakId)
            .getDevices()
            .stream()
            .map(d -> new DeviceDto(
                d.getDeviceId().getIp(),
                d.getDeviceId().getClientKey(),
                d.getName(),
                d.getModel()))
            .toList();
    }

    public void add(String keycloakId, DeviceDto deviceDto) {
        var deviceId = new DeviceId();
        deviceId.setIp(deviceDto.ip());
        deviceId.setClientKey(deviceDto.clientKey());
        var device = new Device(deviceId);
        device.setName(deviceDto.name());
        device.setModel(deviceDto.model());
        getCustomer(keycloakId).getDevices().add(device);
    }

    private Customer getCustomer(String keycloakId) {
        return customerService.findOrCreateByKeycloakId(keycloakId);
    }

    public void saveDevices(final @Nullable String keycloakId, final Collection<DeviceDto> deviceDtos) {
        var customer = getCustomer(keycloakId);
        customer.getDevices().clear();
        deviceDtos.forEach(deviceDto -> add(keycloakId, deviceDto));
    }
}
