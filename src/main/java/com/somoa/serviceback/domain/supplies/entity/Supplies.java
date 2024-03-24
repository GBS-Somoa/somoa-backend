package com.somoa.serviceback.domain.supplies.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.relational.core.mapping.Column;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "supplies")
@Data
public class Supplies {

    @Id
    @Column("supplies_id")
    private int id;

    @Column("supplies_type")
    private String Type;

    @Column("supplies_name")
    private String Name;

    @Column("supplies_change_date")
    private String ChangeDate;

    @Column("supplies_status")
    private String Status;

    @Column("supplies_amount")
    private int amount;

    @Column("supplies_limit")
    private Object limit;

    @Column("supplies_amount_tmp")
    private int amountTmp;
}
