package com.somoa.serviceback.domain.order.dto;

import lombok.Data;

@Data
public class OrderSaveDto {

    private Integer groupId;
    private Integer userId;
    private String supplyId;
    private String orderStatus;
    private String orderStore;
    private String orderStoreId;
    private Integer orderCount;
    private String productName;
    private String productImg;
    private String productBarcode;
}
