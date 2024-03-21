package com.somoa.serviceback.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderSaveDto {
    private Integer groupId;
    private Integer supplyId;
    private String orderStatus;
    private String productName;
    private String orderStore;
    private String orderStoreId;
    private String productImg;
    private Integer orderCount;
    private String orderAmount;
}
