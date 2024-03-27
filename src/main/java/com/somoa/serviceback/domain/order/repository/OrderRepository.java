package com.somoa.serviceback.domain.order.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.order.entity.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Mono<Order> findByOrderStoreIdAndOrderStore(String orderStoreId, String orderStore);

    Flux<Order> findAllByGroupId(Integer groupId);

    @Query("SELECT * " +
          "   FROM `order` " +
          "  WHERE supply_id = :supplyId" +
          "  ORDER BY created_at DESC" +
          "  LIMIT 1")
    Mono<Order> findLatestOrderBySupplyId(String supplyId);
}
