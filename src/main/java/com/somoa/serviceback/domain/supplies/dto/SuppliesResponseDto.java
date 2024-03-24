package com.somoa.serviceback.domain.supplies.dto;

import lombok.Data;

@Data
public class SuppliesResponseDto {

    private int id;

    private String Type;

    private String Name;

    private String ChangeDate;

    private String Status;

    private int amount;

    private Object limit;

    private int amountTmp;
}
