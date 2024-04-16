package com.somoa.serviceback.domain.supply.controller;

import com.somoa.serviceback.domain.device.exception.DeviceNotFoundException;
import com.somoa.serviceback.domain.product.dto.BarcodeRequest;
import com.somoa.serviceback.domain.supply.dto.SupplyAmountParam;
import com.somoa.serviceback.domain.supply.service.SupplyService;
import com.somoa.serviceback.global.annotation.Login;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;
    @GetMapping
    public Mono<ResponseEntity<ResponseHandler>> supplySearch(@RequestParam Integer groupId, @RequestParam Boolean careRequired) {
        return supplyService.searchGroupSupply(groupId, careRequired)
                .collectList() // 모든 Flux 결과를 List로 수집
                .flatMap(suppliesList -> {
                    if (!suppliesList.isEmpty()) {
                        return ResponseHandler.ok(suppliesList, "조건에 해당하는 소모품 목록 조회에 성공했습니다.");
                    } else {
                        return ResponseHandler.error("조건에 맞는 소모품이 없습니다.", HttpStatus.NOT_FOUND);
                    }
                });
    }

    @GetMapping("/barcode")
    public Mono<ResponseEntity<ResponseHandler>> barcodeToProduct(@Login Integer loginUserId, @RequestBody BarcodeRequest barcodeRequest) {
        return supplyService.barcodeToProduct(loginUserId,barcodeRequest)
                .flatMap(resultMap -> ResponseHandler.ok(resultMap, "모든 소모품 목록 조회에 성공했습니다."))
                .onErrorResume(this::handleError);
    }

    @GetMapping("/groupsupply")
    public Mono<ResponseEntity<ResponseHandler>> allSupplybyGroupSearch(@Login Integer loginUserId, @RequestParam Integer groupId) {
        return supplyService.searchAllGroupSupply(loginUserId,groupId)
                .flatMap(resultMap -> ResponseHandler.ok(resultMap, "모든 소모품 목록 조회에 성공했습니다."))
                .onErrorResume(this::handleError);
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ResponseHandler>> allSupplySearch(@Login Integer loginUserId) {
        return supplyService.searchAllSupply(loginUserId)
                .flatMap(resultMap -> ResponseHandler.ok(resultMap, "모든 소모품 목록 조회에 성공했습니다."))
                   .onErrorResume(this::handleError);
    }

    @PatchMapping("/{supplyId}")
    public Mono<ResponseEntity<ResponseHandler>> updateSupply(@PathVariable String supplyId, @RequestBody SupplyAmountParam supplyAmountParam) {
        return supplyService.updateSupply(supplyId, supplyAmountParam.getSupplyAmount())
                .flatMap(data -> ResponseHandler.ok(data, "소모품 수정에 성공하였습니다."))
                .onErrorResume(this::handleError);
    }

    @PatchMapping("/limit/{supplyId}")
    public Mono<ResponseEntity<ResponseHandler>> updateSupplyLimit(@PathVariable String supplyId, @RequestBody Map<String, Object> limitParam) {
        return supplyService.updateSupplyLimit(supplyId, limitParam)
                .flatMap(updatedSupply -> ResponseHandler.ok(updatedSupply, "소모품 알림 기준 용량 수정에 성공하였습니다."))
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
