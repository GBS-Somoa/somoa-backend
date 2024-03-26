package com.somoa.serviceback.global.fcm.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.global.fcm.dto.FcmGroupMessageDto;
import com.somoa.serviceback.global.fcm.dto.FcmMessageDto;
import com.somoa.serviceback.global.fcm.dto.FcmSendDto;
import com.somoa.serviceback.global.fcm.entity.FcmToken;
import com.somoa.serviceback.global.fcm.repository.FcmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final FcmRepository fcmRepository;
    private final GroupUserRepository groupUserRepository;
    private final WebClient webClient = WebClient.builder().build();
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

    public Mono<Integer> sendMessageTo(FcmSendDto fcmSendDto) {
        return Mono.fromCallable(this::getAccessToken)
                .flatMap(token -> webClient.post()
                        .uri("https://fcm.googleapis.com/v1/projects/somoa-8dea6/messages:send")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createMessage(fcmSendDto))
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(error -> System.out.println(error))
                        .map(response -> 1)
                        .onErrorReturn(0));
    }


    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "fcm-admin-sdk.json";
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));;
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }


    private String createMessage(FcmSendDto fcmSendDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        FcmMessageDto fcmMessageDto = FcmMessageDto.builder()
                .message(FcmMessageDto.Message.builder()
                        .token(fcmSendDto.getToken())
                        .notification(FcmMessageDto.Notification.builder()
                                .title(fcmSendDto.getTitle())
                                .body(fcmSendDto.getBody())
                                .build())
                        .build())
                .validateOnly(false)
                .build();
        try {
            return objectMapper.writeValueAsString(fcmMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Creating FCM message failed", e);
        }
    }

    public Mono<Integer> sendMessageToGroup(int groupId, String title, String body) {
        return Mono.fromCallable(this::getAccessToken)
                .flatMap(accessToken ->{
                    System.out.println(accessToken);
                    return groupUserRepository.findUserIdsByGroupId(groupId).collectList()
                            .flatMap(userIds -> {
                                return Flux.fromIterable(userIds)
                                        .flatMap(userId -> fcmRepository.findByUserId(userId))
                                        .collectList()
                                        .flatMap(fcmTokens -> {
                                            // FcmToken에서 token만 추출
                                            List<String> tokens = fcmTokens.stream().map(FcmToken::getToken).collect(Collectors.toList());
                                            // 모든 토큰에 대해 메시지 전송
                                            return sendGroupMessage(tokens, title, body, accessToken);
                                        });
                            });
                }).onErrorReturn(0);
    }

    private Mono<Integer> sendGroupMessage(List<String> tokens, String title, String body, String accessToken) {
        // 메시지 구성
        String messagePayload = createMessagePayload(tokens, title, body);
        System.out.println(messagePayload);
        // FCM에 메시지 전송
        return webClient.post()
                .uri("https://fcm.googleapis.com/v1/projects/somoa-8dea6/messages:send")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(messagePayload)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> 1) // 성공적으로 메시지를 전송했으면 1 반환
                .doOnError(error -> System.err.println("Error sending group message: " + error.getMessage()))
                .onErrorReturn(0); // 오류가 발생하면 0 반환
    }

    private String createMessagePayload(List<String> tokens, String title, String body) {
        try {
            // ObjectMapper를 사용하여 메시지를 JSON 형식으로 변환
            System.out.println("Create!!!");
            ObjectMapper objectMapper = new ObjectMapper();
            FcmGroupMessageDto message = FcmGroupMessageDto.builder()
                    .validateOnly(false)
                    .message(FcmGroupMessageDto.Message.builder()
                            .notification(FcmGroupMessageDto.Notification.builder()
                                    .title(title)
                                    .body(body)
                                    .build())
                            .registrationIds(tokens)
                            .build())
                    .build();
            try {
                return objectMapper.writeValueAsString(message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert message to JSON", e);
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to create message payload", e);
        }
    }
}
