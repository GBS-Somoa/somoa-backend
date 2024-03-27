package com.somoa.serviceback.domain.order.repository;

import com.somoa.serviceback.domain.order.dto.OrderWithGroupnameResponse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.order.entity.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Mono<Order> findByOrderStoreIdAndOrderStore(String orderStoreId, String orderStore);

    Flux<Order> findAllByGroupId(Integer groupId);

    @Query("SELECT o.orderId, o.supplyId, o.orderStatus, o.orderStore, o.orderStoreId, o.productName, o.productImg, o.orderCount, o.orderAmount, o.createdAt, o.updatedAt, g.groupName " +
            "FROM Order o " +
            "INNER JOIN Group g ON o.groupId = g.groupId " +
            "INNER JOIN GroupUser gu ON gu.groupId = g.groupId " +
            "WHERE gu.userId = :userId")
    Flux<OrderWithGroupnameResponse> findByUserIdWithGroupName(Integer userId);
}
