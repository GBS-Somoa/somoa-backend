package com.somoa.serviceback.global.fcm;


import com.somoa.serviceback.domain.group.repository.GroupUserRepository;
import com.somoa.serviceback.global.fcm.entity.FcmToken;
import com.somoa.serviceback.global.fcm.repository.FcmRepository;
import com.somoa.serviceback.global.fcm.service.FcmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class FcmServiceTest {
    @Mock
    private FcmRepository fcmRepository;

    @Mock
    private GroupUserRepository groupUserRepository;

    private FcmService fcmService;


}