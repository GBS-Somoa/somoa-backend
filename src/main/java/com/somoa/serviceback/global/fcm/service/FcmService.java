package com.somoa.serviceback.global.fcm.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.global.fcm.dto.FcmMessageDto;
import com.somoa.serviceback.global.fcm.dto.FcmSendDto;
import com.somoa.serviceback.global.fcm.entity.FcmToken;
import com.somoa.serviceback.global.fcm.repository.FcmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

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
        return findFcmTokensByGroupId(groupId)
                .flatMap(fcmToken -> {
                    FcmSendDto fcmsendDto = new FcmSendDto(fcmToken.getToken(), title, body);
                    return sendMessageTo(fcmsendDto);
                })
                .collectList()
                .map(resultList -> {
                    long successCount = resultList.stream().filter(result -> result == 1).count();
                    return (int) successCount;
                });
    }

    public Flux<FcmToken> findFcmTokensByGroupId(Integer groupId) {
        return groupUserRepository.findAllByGroupId(groupId)
                .flatMap(groupUserResponse -> fcmRepository.findByUserId(groupUserResponse.getUserId()))
                .collectList()
                .flatMapMany(Flux::fromIterable);
    }
}
