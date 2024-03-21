package com.somoa.serviceback.domain.device.controller;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.service.DeviceService;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    private Mono<ResponseEntity<ResponseHandler>> register(@RequestBody DeviceRegisterParam param) {
//        Integer userId = 1;     // Token에서 userId 정보 추출
        return deviceService.save(param)
                .flatMap(data -> ResponseHandler.ok(data, "기기를 등록했습니다."))
                .onErrorResume(error -> {
                    log.error("error occurs!!", error);
                    return ResponseHandler.error("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @PostMapping("/{device_id}")
    public Mono<ResponseEntity<ResponseHandler>> handleDeviceStatus(@PathVariable String device_id) {

        return ResponseHandler.ok(null,"완료");
    }
}
