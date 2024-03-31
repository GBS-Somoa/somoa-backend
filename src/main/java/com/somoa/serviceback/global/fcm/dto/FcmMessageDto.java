package com.somoa.serviceback.global.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmMessageDto {
    private boolean validateOnly;
    private FcmMessageDto.Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {

        private FcmMessageDto.Notification notification;
        private String token;
        private FcmMessageDto.Data data;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class  Data{
        private String icon;
        private String path;
        private String pathData;
        private String groupId;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

}