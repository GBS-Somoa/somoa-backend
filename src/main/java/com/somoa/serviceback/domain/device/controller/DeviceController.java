package com.somoa.serviceback.domain.device.controller;

import com.somoa.serviceback.domain.device.dto.DeviceUpdateParam;
import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.service.DeviceService;
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
    private Mono<ResponseEntity<ResponseHandler>> register(@RequestBody DeviceRegisterParam param) {
//        Integer userId = 1;     // Token에서 userId 정보 추출
        return deviceService.save(param)
                .flatMap(data -> ResponseHandler.ok(data,"기기를 등록했습니다."))
                .onErrorResume(this::handleError);
    }

    @GetMapping("/{deviceId}")
    private Mono<ResponseEntity<ResponseHandler>> getDevice(@PathVariable String deviceId) {
        return deviceService.findById(deviceId)
                .flatMap(data -> ResponseHandler.ok(data, "기기 정보를 조회했습니다."))
                .onErrorResume(this::handleError);
    }

    @PatchMapping("/{deviceId}")
    private Mono<ResponseEntity<ResponseHandler>> updateDevice(@PathVariable String deviceId, @RequestBody DeviceUpdateParam param) {
        return deviceService.update(deviceId, param)
                .flatMap(ResponseHandler::noContent)
                .onErrorResume(this::handleError);
    }

    @DeleteMapping("/{deviceId}")
    private Mono<ResponseEntity<ResponseHandler>> deleteDevice(@PathVariable String deviceId) {
        return deviceService.delete(deviceId)
                .flatMap(ResponseHandler::noContent)
                .onErrorResume(this::handleError);
    }

    private Mono<ResponseEntity<ResponseHandler>> handleError(Throwable error) {
        if (error instanceof DeviceNotFoundException) {
            return ResponseHandler.error(error.getMessage(), HttpStatus.NOT_FOUND);
        } else {
            log.error("error occurs!!", error);
            return ResponseHandler.error(error.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
