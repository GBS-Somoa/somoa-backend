package com.somoa.serviceback.domain.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.order.entity.Order;

import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Mono<Order> findByOrderStoreId(String orderStoreId);
}
