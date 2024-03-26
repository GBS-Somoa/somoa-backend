package com.somoa.serviceback.global.fcm.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "fcmtoken")
@CompoundIndex(def = "{'user_id': 1, 'mobile_device_id': 1}", name = "user_device_index", unique = true)
public class FcmToken {

    @Id
    private String id;

    @Field("user_id")
    private int userId;

    @Field("mobile_device_id")
    private String mobileDeviceId;

    @Field("token")
    private String token;

}