package com.somoa.serviceback.domain.order.controller;

import com.somoa.serviceback.domain.order.dto.OrderSaveDto;
import com.somoa.serviceback.domain.order.dto.OrderStatusUpdateDto;
import com.somoa.serviceback.domain.order.service.OrderService;
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

    @PatchMapping("/{order_store}/{order_id}")
    public Mono<ResponseEntity<ResponseHandler>> updateOrderStatus(@PathVariable("order_store") String orderStore, @PathVariable("order_id") String orderId, @RequestBody OrderStatusUpdateDto orderStatusUpdateDto) {
        return orderService.updateOrderStatus(orderStore, orderId, orderStatusUpdateDto)
                .flatMap(order -> ResponseHandler.ok(null, "주문 상태 변경에 성공했습니다."))
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return ResponseHandler.error(error.getMessage(), HttpStatus.BAD_REQUEST);
                    }
                    log.error("주문 상태 변경에 실패했습니다.", error);
                    return ResponseHandler.error("서버 오류로 주문 상태 변경에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping
    public Mono<ResponseEntity<ResponseHandler>> getOrders(@RequestParam(value = "supply_id") String supplyId,
                                                           @RequestParam(value = "order_status", required = false) String orderStatus,
                                                           @RequestParam(value = "size", defaultValue = "5") int size) {
        return orderService.findOrders(supplyId, orderStatus, size)
                .flatMap(data -> ResponseHandler.ok(data, "주문 목록을 조회했습니다."))
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return ResponseHandler.error(error.getMessage(), HttpStatus.BAD_REQUEST);
                    }
                    return ResponseHandler.error("internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
