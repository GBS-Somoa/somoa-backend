package com.somoa.serviceback.domain.device.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.somoa.serviceback.domain.device.dto.DeviceApiStatusResponse;
import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.dto.DeviceUpdateParam;
import com.somoa.serviceback.domain.device.service.DeviceService;
import com.somoa.serviceback.global.annotation.Login;
import com.somoa.serviceback.global.handler.ResponseHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public Mono<ResponseEntity<ResponseHandler>> register(@RequestBody DeviceRegisterParam param) {
        return deviceService.save(param)
            .flatMap(data -> ResponseHandler.ok(data, "기기를 등록했습니다."));
    }

    @GetMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> findOne(@PathVariable String deviceId) {
        return deviceService.findById(deviceId)
            .flatMap(data -> ResponseHandler.ok(data, "기기 정보를 조회했습니다."));
    }

    @PatchMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> update(@Login Integer loginUserId,
                                                        @PathVariable String deviceId,
                                                        @RequestBody DeviceUpdateParam param) {
        return deviceService.update(loginUserId, deviceId, param)
            .then(ResponseHandler.ok("기기 정보를 수정했습니다."));
    }

    @DeleteMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> delete(@Login Integer loginUserId,
                                                        @PathVariable String deviceId) {
        return deviceService.delete(loginUserId, deviceId)
            .then(ResponseHandler.ok("기기를 삭제했습니다."));
    }

    @PostMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> handleDeviceStatus(@PathVariable String deviceId, @RequestBody DeviceApiStatusResponse deviceApiStatusResponse) {
        System.out.println(deviceApiStatusResponse.toString());
        return deviceService.statusUpdate(deviceId, deviceApiStatusResponse)
            .then(ResponseHandler.ok("기기 상태를 업데이트했습니다."));
    }
}
