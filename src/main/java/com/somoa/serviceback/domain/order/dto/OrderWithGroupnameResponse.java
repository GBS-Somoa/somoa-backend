package com.somoa.serviceback.domain.order.dto;

import com.somoa.serviceback.domain.order.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderWithGroupnameResponse {

    private Integer orderId;
    private String supplyId;
    private String orderStatus;
    private String orderStore;
    private String orderStoreId;
    private String productName;
    private String productImg;
    private int orderCount;
    private String orderAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderWithGroupnameResponse of(Order order) {
        return OrderWithGroupnameResponse.builder()
                .orderId(order.getId())
                .supplyId(order.getSupplyId())
                .orderStatus(order.getOrderStatus())
                .orderStore(order.getOrderStore())
                .orderStoreId(order.getOrderStoreId())
                .productName(order.getProductName())
                .productImg(order.getProductImg())
                .orderCount(order.getOrderCount())
                .orderAmount(order.getOrderAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
