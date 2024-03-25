package com.somoa.serviceback.domain.supplies.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "supplies")
@Data
public class Supplies {

    @Id
    @Field("supplies_id")
    private int id;

    @Field("supplies_type")
    private String Type;

    @Field("supplies_name")
    private String Name;

    @Field("supplies_change_date")
    private String ChangeDate;

    @Field("supplies_status")
    private String Status;

    @Field("supplies_amount")
    private int amount;

    @Field("supplies_limit")
    private Limit limit;

    @Field("supplies_amount_tmp")
    private int amountTmp;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Limit {

        /**
         * type : duration, status, amount
         * value :
         * duration 일떄: 알림기준날짜(ex, 6개월 등 )(필터등  사용시),
         * status 일때: 알림기준상태(ex,"normal" "bad", "good",  등),
         * amount 일때: 알림기준용량
         */
        private String type;
        private Object value;

        public Limit createLimit(String type, Object defaultValue) {
            Limit limit = new Limit();
            limit.setType(type);
            limit.setValue(defaultValue);
            return limit;
        }
    }
}
