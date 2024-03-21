package com.somoa.serviceback.domain.device.controller;

import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    @PostMapping("/{device_id}")
    public Mono<ResponseEntity<ResponseHandler>> handleDeviceStatus(@PathVariable String device_id) {

        return ResponseHandler.ok(null,"완료");
    }
}
