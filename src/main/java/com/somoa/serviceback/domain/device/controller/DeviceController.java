package com.somoa.serviceback.domain.device.controller;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.device.dto.DeviceStatusDto;
import com.somoa.serviceback.domain.device.service.DeviceService;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    /**
     * 기기상태 업데이트 및 알람주기
     * 기기id를 받아와서, 해당 기기의 소모품들을 가져와서, 상태 업데이트 시키고, 기준치 이하일 경우 알람주기
     * @param device_id
     * @param deviceStatusDto
     * @return
     */
    @PostMapping("/{device_id}")
    public Mono<ResponseEntity<ResponseHandler>> handleDeviceStatus(@PathVariable String device_id, @RequestBody DeviceStatusDto deviceStatusDto) {
        System.out.println(deviceStatusDto.toString());
        return deviceService.StatusUpdate(device_id, deviceStatusDto)
                .flatMap(data -> ResponseHandler.ok(data, "기기 상태를 업데이트했습니다."))
                .onErrorResume(error -> {
                    log.error("에러발생", error);
                    return ResponseHandler.error("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
