package com.somoa.serviceback.domain.order.service;

import com.somoa.serviceback.domain.order.dto.OrderSaveDto;
import com.somoa.serviceback.domain.order.dto.OrderStatusUpdateDto;
import com.somoa.serviceback.domain.order.entity.Order;
import com.somoa.serviceback.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Mono<Object> saveOrder(OrderSaveDto orderSaveDto) {
        return orderRepository.findByOrderStoreId(orderSaveDto.getOrderStoreId())
                .flatMap(existingOrder -> Mono.error(new IllegalArgumentException("Order already exists.")))
                .switchIfEmpty(Mono.defer(() -> {
                            Order order = Order.builder()
                                    .groupId(orderSaveDto.getGroupId())
                                    .supplyId(orderSaveDto.getSupplyId())
                                    .orderStatus(orderSaveDto.getOrderStatus())
                                    .productName(orderSaveDto.getProductName())
                                    .orderStore(orderSaveDto.getOrderStore())
                                    .orderStoreId(orderSaveDto.getOrderStoreId())
                                    .productImg(orderSaveDto.getProductImg())
                                    .orderCount(orderSaveDto.getOrderCount())
                                    .orderAmount(orderSaveDto.getOrderAmount())
                                    .build();
                            return orderRepository.save(order)
                                    .map(savedOrder -> Mono.empty());
                        }
                ));
    }

    @Transactional
    public Mono<Order> updateOrderStatus(String orderStoreId, OrderStatusUpdateDto orderStatusUpdateDto) {
        return orderRepository.findByOrderStoreId(orderStoreId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Order not found with id: " + orderStoreId)))
                .flatMap(order -> {
                    order.setOrderStatus(orderStatusUpdateDto.getOrderStatus());
                    return orderRepository.save(order);
                });
    }
}