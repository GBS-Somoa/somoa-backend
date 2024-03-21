package com.somoa.serviceback.domain.order.repository;

import com.somoa.serviceback.domain.order.entity.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {
    Mono<Order> findByOrderStoreId(String orderStoreId);
}
