package com.somoa.serviceback.domain.supplies.controller;

import com.somoa.serviceback.domain.device.dto.DeviceRegisterParam;
import com.somoa.serviceback.domain.supplies.service.SuppliesService;
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
@RequestMapping("/supplies")
public class SuppliesController {

    private final SuppliesService suppliesService;
    @GetMapping
    public Mono<ResponseEntity<ResponseHandler>> suppliesSearch(@RequestParam Integer groupId, @RequestParam Boolean careRequired) {
        return suppliesService.findSuppliesByGroupIdAndCareRequired(groupId, careRequired)
                .collectList() // 모든 Flux 결과를 List로 수집
                .flatMap(suppliesList -> {
                    if (!suppliesList.isEmpty()) {
                        return ResponseHandler.ok(suppliesList, "조건에 해당하는 소모품 목록 조회에 성공했습니다.");
                    } else {
                        return ResponseHandler.error("조건에 맞는 소모품이 없습니다.", HttpStatus.NOT_FOUND);
                    }
                });
    }

}
