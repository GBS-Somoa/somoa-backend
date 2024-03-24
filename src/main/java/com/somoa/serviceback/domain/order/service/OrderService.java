package com.somoa.serviceback.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.order.dto.OrderSaveDto;
import com.somoa.serviceback.domain.order.dto.OrderStatusUpdateDto;
import com.somoa.serviceback.domain.order.entity.Order;
import com.somoa.serviceback.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Mono<Map<String, Object>> saveOrder(OrderSaveDto orderSaveDto) {
        return orderRepository.findByOrderStoreId(orderSaveDto.getOrderStoreId())
                .hasElement()
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("이미 등록된 주문입니다.")))
                .then(orderRepository.save(Order.builder()
                        .groupId(orderSaveDto.getGroupId())
                        .supplyId(orderSaveDto.getSupplyId())
                        .orderStatus(orderSaveDto.getOrderStatus())
                        .productName(orderSaveDto.getProductName())
                        .orderStore(orderSaveDto.getOrderStore())
                        .orderStoreId(orderSaveDto.getOrderStoreId())
                        .productImg(orderSaveDto.getProductImg())
                        .orderCount(orderSaveDto.getOrderCount())
                        .orderAmount(orderSaveDto.getOrderAmount())
                        .build()))
                // TODO: supplyId에 해당하는 소모품 임시 용량 추가
//                .flatMap(order -> {
//                    return supplyRepository.findById(order.getSupplyId())
//                            .flatMap(supply -> {
//                                supply.setAmountTmp(supply.getAmountTmp() + order.getOrderCount());
//                                return supplyRepository.save(supply);
//                            })
//                            .thenReturn(order);
//                })
                .map(order -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", order.getId());
                    return response;
                });
    }

    @Transactional
    public Mono<Order> updateOrderStatus(String orderStoreId, OrderStatusUpdateDto orderStatusUpdateDto) {
        return orderRepository.findByOrderStoreId(orderStoreId)
                .flatMap(order -> {
                    order.setOrderStatus(orderStatusUpdateDto.getOrderStatus());
                    return orderRepository.save(order);
                })
                // TODO: 주문 상태 = 배송 완료로 변경 시 supplyId에 해당하는 소모품 임시 용량 값 -> 실제 용량 값에 반영
                // TODO: FCM 알림 전송
//                .flatMap(order -> {
//                    if ("배송 완료".equals(order.getOrderStatus())) {
//                        return supplyRepository.findById(order.getSupplyId())
//                                .flatMap(supply -> {
//                                    supply.setAmount(supply.getAmount() + supply.getAmountTmp());
//                                    supply.setAmountTmp(0);
//                                    return supplyRepository.save(supply);
//                                })
//                                .thenReturn(order);
//                    } else {
//                        return Mono.just(order);
//                    }
//                })
//                .flatMap(order -> {
//                    return fcmService.sendNotification(order.getOrderStatus())
//                            .thenReturn(order);
//                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다.")));
    }
}
