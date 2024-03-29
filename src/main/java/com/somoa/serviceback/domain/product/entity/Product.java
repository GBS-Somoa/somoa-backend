package com.somoa.serviceback.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("product")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @Column("product_id")
    private int id;

    @Column("product_name")
    private String name;

    @Column("product_amount")
    private String amount;

    @Column("product_barcode")
    private String barcode;

    @Column("product_price")
    private int price;

    @Column("product_img")
    private String image;

    @Column("product_type")
    private String type;
}
