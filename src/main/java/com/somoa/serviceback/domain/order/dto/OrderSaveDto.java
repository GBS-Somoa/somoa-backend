package com.somoa.serviceback.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderSaveDto {

    private Integer groupId;
    @JsonProperty("userId")
    private String userName;
    private String supplyId;
    private String orderStatus;
    private String orderStore;
    private String orderStoreId;
    private Integer orderCount;
    private String productName;
    private String productImg;
    private String productBarcode;
}
