package com.somoa.serviceback.domain.order.repository;

import com.somoa.serviceback.domain.order.entity.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Mono<Order> findByOrderStoreIdAndOrderStore(String orderStoreId, String orderStore);

    Flux<Order> findAllByGroupId(Integer groupId);

    @Query("SELECT * " +
            "   FROM `order` " +
            "  WHERE supply_id = :supplyId AND" +
            "        (:orderStatus IS NULL OR (order_status = :orderStatus))" +
            "  ORDER BY created_at DESC" +
            "  LIMIT :count")
    Flux<Order> findOrders(String supplyId, String orderStatus, int count);
}
