package com.ssdc.serviceback.domain.user.controller;

import com.ssdc.serviceback.domain.user.dto.UserSignupDto;
import com.ssdc.serviceback.domain.user.service.UserService;
import com.ssdc.serviceback.global.auth.AuthConverter;
import com.ssdc.serviceback.global.auth.JwtService;
import com.ssdc.serviceback.global.auth.SecurityConfig;
import com.ssdc.serviceback.global.handler.ResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserController.class)
@DisplayName("유저 컨트롤러 테스트")
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthConverter authConverter;

    //@MockBean
   // private SecurityConfig securityConfig;

    @MockBean
    private UserService userService;

    private UserSignupDto userSignupDto;

    @BeforeEach
    void setUp() {
        userSignupDto = new UserSignupDto("testUser", "password", "testNickname");
    }

    @Test
    void signUp() {
        // Given
        Mockito.when(userService.signUp(Mockito.any(UserSignupDto.class)))
                .thenReturn(Mono.just("User Created"));

        // When & Then
        webTestClient
                .post().uri("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSignupDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("회원가입이 성공적으로 완료되었습니다.");
    }
}
