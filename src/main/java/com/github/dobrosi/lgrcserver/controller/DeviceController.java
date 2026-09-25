package com.github.dobrosi.lgrcserver.controller;

import java.util.Collection;
import java.util.List;

import com.github.dobrosi.lgrcserver.dto.DeviceDto;
import com.github.dobrosi.lgrcserver.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceController {
    private final DeviceService deviceService;

    @Autowired
    public DeviceController(final DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<DeviceDto>> getDevices(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(deviceService.getDevices(jwt.getSubject()), HttpStatus.OK);
    }

    @GetMapping("/save")
    public void saveDavices(@AuthenticationPrincipal Jwt jwt, Collection<DeviceDto> deviceDtos) {
        deviceService.saveDevices(jwt.getSubject(), deviceDtos);
    }

    @GetMapping("/add")
    public void add(@AuthenticationPrincipal Jwt jwt, DeviceDto deviceDto) {
        deviceService.add(jwt.getSubject(), deviceDto);
    }
}
