package com.somoa.serviceback.domain.order.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("`order`")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @Column("order_id")
    private int id;

    @Column("group_id")
    private int groupId;

    @Column("supply_id")
    private String supplyId;

    @Column("order_status")
    private String orderStatus;

    @Column("product_name")
    private String productName;

    @Column("order_store")
    private String orderStore;

    @Column("order_store_id")
    private String orderStoreId;

    @Column("product_img")
    private String productImg;

    @Column("order_count")
    private int orderCount;

    @Column("order_amount")
    private String orderAmount;

    @Column("created_at")
    private LocalDateTime createdAt;
}
