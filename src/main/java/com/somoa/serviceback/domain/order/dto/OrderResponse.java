package com.somoa.serviceback.domain.order.dto;

import com.somoa.serviceback.domain.order.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {

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

	public static OrderResponse of(Order order) {
		return OrderResponse.builder()
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
			.build();
	}
}
