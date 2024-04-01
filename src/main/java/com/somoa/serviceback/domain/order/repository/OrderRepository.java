package com.somoa.serviceback.domain.order.repository;

import org.springframework.data.r2dbc.repository.Query;
import com.somoa.serviceback.domain.order.dto.OrderWithGroupnameResponse;
import com.somoa.serviceback.domain.order.entity.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.somoa.serviceback.domain.order.entity.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Mono<Order> findByOrderStoreIdAndOrderStore(String orderStoreId, String orderStore);

    Flux<Order> findAllByGroupId(Integer groupId);

    @Query("SELECT o.order_id, o.supply_id, o.order_status, o.order_store, o.order_store_id, o.product_name, o.product_img, o.order_count, o.order_amount, o.created_at, o.updated_at, g.group_name " +
            "FROM `order` o " +
            "INNER JOIN `group` g ON o.group_id = g.group_id " +
            "INNER JOIN `group_user` gu ON gu.group_id = g.group_id " +
            "WHERE gu.user_id = :userId " +
            "AND (o.order_status = '배송 중' OR o.order_status = '주문 완료') " +
            "ORDER BY o.created_at DESC")
    Flux<OrderWithGroupnameResponse> findByUserIdWithGroupName(Integer userId);

    @Query("SELECT * " +
            "   FROM `order` " +
            "  WHERE supply_id = :supplyId" +
            "  ORDER BY created_at DESC" +
            "  LIMIT 1")
    Mono<Order> findLatestOrderBySupplyId(String supplyId);

    @Query("SELECT * " +
            "   FROM `order` " +
            "  WHERE supply_id = :supplyId AND" +
            "        (:orderStatus IS NULL OR (order_status = :orderStatus))" +
            "  ORDER BY created_at DESC" +
            "  LIMIT :count")
    Flux<Order> findOrders(String supplyId, String orderStatus, int count);

    @Query("SELECT * " +
            "   FROM `order` " +
            "  WHERE supply_id = :supplyId AND" +
            "        (order_status = '주문 완료' OR order_status = '배송 중')" +
            "  ORDER BY created_at DESC")
    Flux<Order> findOrdersInProgress(String supplyId);
}
