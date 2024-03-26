package com.somoa.serviceback.domain.order.dto;

import com.somoa.serviceback.domain.order.entity.Order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

	private Integer orderId;
	private Integer supplyId;
	private String orderStatus;
	private String orderStore;
	private String orderStoreId;
	private String productName;
	private String productImg;
	private int orderCount;
	private String orderAmount;

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
			.build();
	}
}
