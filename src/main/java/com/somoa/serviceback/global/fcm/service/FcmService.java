package com.somoa.serviceback.global.fcm.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.somoa.serviceback.global.fcm.dto.FcmMessageDto;
import com.somoa.serviceback.global.fcm.dto.FcmSendDto;
import com.somoa.serviceback.global.fcm.entity.FcmToken;
import com.somoa.serviceback.global.fcm.repository.FcmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final FcmRepository fcmRepository;

    public Mono<FcmToken> saveOrUpdateFcmToken(int userId, String mobileDeviceId, String fcmToken) {
        return fcmRepository.findByUserIdAndMobileDeviceId(userId, mobileDeviceId)
                .flatMap(existingToken -> {
                    existingToken.setToken(fcmToken);
                    return fcmRepository.save(existingToken);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    FcmToken newToken = FcmToken.builder()
                            .userId(userId)
                            .mobileDeviceId(mobileDeviceId)
                            .token(fcmToken)
                            .build();
                    return fcmRepository.save(newToken);
                }));
    }

    public int sendMessageTo(FcmSendDto fcmSendDto) throws IOException {

        String message = makeMessage(fcmSendDto);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());

        HttpEntity entity = new HttpEntity<>(message, headers);

        String API_URL = "https://fcm.googleapis.com/v1/projects/somoa-8dea6/messages:send";
        ResponseEntity response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

        System.out.println(response.getStatusCode());

        return response.getStatusCode() == HttpStatus.OK ? 1 : 0;
    }


    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "fcm-admin-sdk.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    private String makeMessage(FcmSendDto fcmSendDto) throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper();
        FcmMessageDto fcmMessageDto = FcmMessageDto.builder()
                .message(FcmMessageDto.Message.builder()
                        .token(fcmSendDto.getToken())
                        .notification(FcmMessageDto.Notification.builder()
                                .title(fcmSendDto.getTitle())
                                .body(fcmSendDto.getBody())
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return om.writeValueAsString(fcmMessageDto);
    }
}
