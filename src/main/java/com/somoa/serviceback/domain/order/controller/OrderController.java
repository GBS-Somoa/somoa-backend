package com.somoa.serviceback.domain.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.somoa.serviceback.domain.order.dto.OrderSaveDto;
import com.somoa.serviceback.domain.order.dto.OrderStatusUpdateDto;
import com.somoa.serviceback.domain.order.service.OrderService;
import com.somoa.serviceback.global.handler.ResponseHandler;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity<ResponseHandler>> saveOrder(@RequestBody OrderSaveDto orderSaveDto) {
        return orderService.saveOrder(orderSaveDto)
                .flatMap(order -> ResponseHandler.ok(order, "주문 등록에 성공했습니다."))
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return ResponseHandler.error(error.getMessage(), HttpStatus.BAD_REQUEST);
                    }
                    log.error("주문 등록에 실패했습니다.", error);
                    return ResponseHandler.error("서버 오류로 주문 등록에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @PatchMapping("/{order_id}")
    public Mono<ResponseEntity<ResponseHandler>> updateOrderStatus(@PathVariable("order_id") String orderId, @RequestBody OrderStatusUpdateDto orderStatusUpdateDto) {
        return orderService.updateOrderStatus(orderId, orderStatusUpdateDto)
                .flatMap(order -> ResponseHandler.ok(null, "주문 상태 변경에 성공했습니다."))
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return ResponseHandler.error(error.getMessage(), HttpStatus.BAD_REQUEST);
                    }
                    log.error("주문 상태 변경에 실패했습니다.", error);
                    return ResponseHandler.error("서버 오류로 주문 상태 변경에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
