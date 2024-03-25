package com.somoa.serviceback.domain.supply.controller;

import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
import com.somoa.serviceback.domain.supply.service.SupplyService;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;
    @GetMapping
    public Mono<ResponseEntity<ResponseHandler>> supplySearch(@RequestParam Integer groupId, @RequestParam Boolean careRequired) {
        return supplyService.searchSupply(groupId, careRequired)
                .collectList() // 모든 Flux 결과를 List로 수집
                .flatMap(suppliesList -> {
                    if (!suppliesList.isEmpty()) {
                        return ResponseHandler.ok(suppliesList, "조건에 해당하는 소모품 목록 조회에 성공했습니다.");
                    } else {
                        return ResponseHandler.error("조건에 맞는 소모품이 없습니다.", HttpStatus.NOT_FOUND);
                    }
                });
    }

    /**
     * Todo: 소모품 수정시, tmp와 실제양 변경 언제이루어지는지?
     * @param supplyId
     * @param supplyAmount
     * @return
     */
    @PatchMapping("/{supplyId}")
    public Mono<ResponseEntity<ResponseHandler>> updateSupply(@PathVariable String supplyId, @RequestBody Integer supplyAmount) {
        return supplyService.updateSupply(supplyId, supplyAmount)
                .flatMap(data -> ResponseHandler.ok(data, "소모품 수정에 성공하였습니다."))
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
