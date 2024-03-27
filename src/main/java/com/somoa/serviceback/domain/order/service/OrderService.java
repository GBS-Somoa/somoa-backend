package com.somoa.serviceback.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.somoa.serviceback.domain.order.entity.Order;
import com.somoa.serviceback.domain.order.dto.OrderSaveDto;
import com.somoa.serviceback.domain.order.dto.OrderStatusUpdateDto;
import com.somoa.serviceback.domain.order.repository.OrderRepository;
import com.somoa.serviceback.domain.supply.repository.SupplyRepository;
import com.somoa.serviceback.global.fcm.service.FcmService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final SupplyRepository supplyRepository;
    final FcmService fcmService;

    public int parseOrderAmount(String orderAmount) {
        if (orderAmount.endsWith("L")) {
            String amountInLiters = orderAmount.substring(0, orderAmount.length() - 1);
            return Integer.parseInt(amountInLiters) * 1000;
        } else if (orderAmount.endsWith("ml")) {
            String amountInMilliliters = orderAmount.substring(0, orderAmount.length() - 2);
            return Integer.parseInt(amountInMilliliters);
        } else {
            throw new IllegalArgumentException("Invalid order amount: " + orderAmount);
        }
    }

    @Transactional
    public Mono<Map<String, Object>> saveOrder(OrderSaveDto orderSaveDto) {
        return orderRepository.findByOrderStoreIdAndOrderStore(orderSaveDto.getOrderStoreId(), orderSaveDto.getOrderStore())
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
                .flatMap(order -> {
                    return supplyRepository.findById(order.getSupplyId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("소모품을 찾을 수 없습니다.")))
                            .flatMap(supply -> {
                                int orderAmountInMilliliters = parseOrderAmount(order.getOrderAmount());
                                if(supply.getDetails().containsKey("supplyAmount")) {
                                    supply.setSupplyAmountTmp(supply.getSupplyAmountTmp() + (order.getOrderCount() * orderAmountInMilliliters));
                                }
                                return supplyRepository.save(supply);
                            })
                            .thenReturn(order);
                })
                .flatMap(order -> {
                    return fcmService.sendMessageToGroup(order.getGroupId(), "새로운 주문", order.getOrderStore() + "에서 " + order.getProductName() + " 주문을 완료하였습니다.")
                            .then(Mono.just(order));
                })
                .map(order -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", order.getId());
                    return response;
                });
    }

    @Transactional
    public Mono<Order> updateOrderStatus(String orderStore, String orderStoreId, OrderStatusUpdateDto orderStatusUpdateDto) {
        return orderRepository.findByOrderStoreIdAndOrderStore(orderStoreId, orderStore)
                .flatMap(order -> {
                    System.out.println("1");
                    Order updatedOrder = Order.builder()
                            .id(order.getId())
                            .groupId(order.getGroupId())
                            .supplyId(order.getSupplyId())
                            .orderStatus(orderStatusUpdateDto.getOrderStatus())
                            .productName(order.getProductName())
                            .orderStore(order.getOrderStore())
                            .orderStoreId(order.getOrderStoreId())
                            .productImg(order.getProductImg())
                            .orderCount(order.getOrderCount())
                            .orderAmount(order.getOrderAmount())
                            // createdAt과 updatedAt은 빌더에 포함하지 않음
                            .build();
                    return orderRepository.save(updatedOrder);
                })
                .flatMap(order -> {
                    System.out.println("2.5");
                    if ("배송 완료".equals(order.getOrderStatus())) {
                        System.out.println("3");
                        return supplyRepository.findById(order.getSupplyId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("소모품을 찾을 수 없습니다.")))
                                .flatMap(supply -> {
                                    System.out.println("4");
                                    if(supply.getDetails().containsKey("supplyAmount")) {
                                        Integer amount = (Integer) supply.getDetails().get("supplyAmount");
                                        supply.getDetails().put("supplyAmount", amount + supply.getSupplyAmountTmp());
                                        supply.setSupplyAmountTmp(0);
                                    }
                                    return supplyRepository.save(supply);
                                })
                                .thenReturn(order);
                    } else if ("주문 취소".equals(order.getOrderStatus())) {
                        System.out.println("5");
                        return supplyRepository.findById(order.getSupplyId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("소모품을 찾을 수 없습니다.")))
                                .flatMap(supply -> {
                                    if(supply.getDetails().containsKey("supplyAmount")) {
                                        supply.setSupplyAmountTmp(0);
                                    }
                                    return supplyRepository.save(supply);
                                })
                                .thenReturn(order);
                    } else {
                        return Mono.just(order);
                    }
                })
                .flatMap(order -> {
                    String title;
                    if ("주문 취소".equals(order.getOrderStatus())) {
                        title = "주문 상태 변경";
                    } else {
                        title = "배송 상태 변경";
                    }
                    return fcmService.sendMessageToGroup(order.getGroupId(), title, order.getOrderStatus() + " : " + order.getOrderStore() + "에서 주문한 " + order.getProductName())
                            .then(Mono.just(order));
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다.")));
    }
}
