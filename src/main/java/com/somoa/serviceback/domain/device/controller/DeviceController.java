package com.somoa.serviceback.domain.device.controller;

import com.somoa.serviceback.domain.device.dto.DeviceApiStatusResponse;
import com.somoa.serviceback.domain.device.dto.DeviceUpdateParam;
import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
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
    private Integer userId = 1;

    @PostMapping
    public Mono<ResponseEntity<ResponseHandler>> register(@RequestBody DeviceRegisterParam param) {
//        Integer userId = 1;     // Token에서 userId 정보 추출
        return deviceService.save(param)
                .flatMap(data -> ResponseHandler.ok(data,"기기를 등록했습니다."))
                .onErrorResume(this::handleError);
    }

    @GetMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> findOne(@PathVariable String deviceId) {
        return deviceService.findById(deviceId)
                .flatMap(data -> ResponseHandler.ok(data, "기기 정보를 조회했습니다."))
                .onErrorResume(this::handleError);
    }

    @PatchMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> update(@PathVariable String deviceId, @RequestBody DeviceUpdateParam param) {
        return deviceService.update(userId, deviceId, param)
                .flatMap(data -> ResponseHandler.ok("기기 정보를 수정했습니다."))
                .onErrorResume(this::handleError);
    }

    @DeleteMapping("/{deviceId}")
    public Mono<ResponseEntity<ResponseHandler>> delete(@PathVariable String deviceId) {
        return deviceService.delete(userId, deviceId)
                .flatMap(data -> ResponseHandler.ok("기기를 삭제했습니다."))
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

    @PostMapping("/{device_id}")
    public Mono<ResponseEntity<ResponseHandler>> handleDeviceStatus(@PathVariable String device_id, @RequestBody DeviceApiStatusResponse deviceApiStatusResponse) {
        System.out.println(deviceApiStatusResponse.toString());
        return deviceService.statusUpdate(device_id, deviceApiStatusResponse)
                .flatMap(data -> ResponseHandler.ok(data, "기기 상태를 업데이트했습니다."))
                .onErrorResume(error -> {
                    log.error("에러발생", error);
                    return ResponseHandler.error("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
