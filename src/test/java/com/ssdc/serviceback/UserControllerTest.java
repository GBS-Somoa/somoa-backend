package com.ssdc.serviceback;

import com.ssdc.serviceback.domain.user.controller.UserController;
import com.ssdc.serviceback.domain.user.dto.UserLoginDto;
import com.ssdc.serviceback.domain.user.dto.UserSignupDto;
import com.ssdc.serviceback.domain.user.repository.UserRepository;
import com.ssdc.serviceback.domain.user.service.UserService;
import com.ssdc.serviceback.global.auth.*;
import com.ssdc.serviceback.global.handler.ResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserController.class,properties="spring.profiles.active=local")
@ContextConfiguration(classes={SecurityConfig.class})
@DisplayName("유저 컨트롤러 테스트")
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserRepository userRepository;


    @MockBean
    private AuthConverter authConverter;

    @MockBean
    private AuthManager authManager;

    @MockBean
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @MockBean
    private JwtService jwtService;


    @MockBean
    private ReactiveUserDetailsService users;
    @MockBean
    private UserService userService;

    private UserSignupDto userSignupDto;
    private UserLoginDto userLoginDto;
    private String validToken;


    @BeforeEach
    void setUp() {

        userSignupDto = new UserSignupDto("testUser", "password", "testNickname");
        userLoginDto = new UserLoginDto("testUser", "password");
        validToken = "Bearer valid.token";

        Mockito.when(userService.signUp(Mockito.any(UserSignupDto.class)))
                .thenReturn(Mono.just(Map.of("id", 1)));

        Mockito.when(userService.loginUser(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(Map.of("accessToken", "access.token", "refreshToken", "refresh.token")));

        Mockito.when(userService.refreshAccessToken(Mockito.anyString()))
                .thenReturn(Mono.just(Map.of("accessToken", "new.access.token", "refreshToken", "new.refresh.token")));
    }

    @Test
    void signUpSuccess() {

        webTestClient.post().uri("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSignupDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.id").isNotEmpty();
    }

    @Test
    void loginSuccess() {
        webTestClient.post().uri("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userLoginDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.accessToken").isNotEmpty();
    }

    @Test
    void refreshAccessTokenSuccess() {
        webTestClient.post().uri("/user/refresh")
                .header("Authorization", validToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.accessToken").isNotEmpty();
    }

    @Test
    void refreshToken() {
    }

    @Test
    void login() {
    }

    @Test
    void auth() {
    }

    @Test
    void checkBlocking() {
    }

    @Test
    void testSignUp() {
    }
}
