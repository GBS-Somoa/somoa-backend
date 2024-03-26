package com.somoa.serviceback.global.fcm.repository;

import com.somoa.serviceback.global.fcm.entity.FcmToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FcmRepository extends ReactiveMongoRepository<FcmToken, String> {

    Mono<FcmToken> findByUserIdAndMobileDeviceId(int userId, String mobileDeviceId);

    Flux<FcmToken> findByUserId(int userId);
}