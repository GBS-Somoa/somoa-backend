package com.somoa.serviceback.global.fcm.dto;

import lombok.*;

import java.util.List;

@Getter
@ToString
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmSendGroupDto {
    private List<String> tokens; // 다중 토큰을 저장할 필드
    private String title;
    private String body;
}
