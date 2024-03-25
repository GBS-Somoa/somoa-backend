package com.somoa.serviceback.domain.order.dto;

import lombok.Data;

@Data
public class OrderSaveDto {

    private Long groupId;
    private Integer supplyId;
    private String orderStatus;
    private String productName;
    private String orderStore;
    private String orderStoreId;
    private String productImg;
    private Integer orderCount;
    private String orderAmount;
}
