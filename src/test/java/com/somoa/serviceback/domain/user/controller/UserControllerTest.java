package com.somoa.serviceback.domain.user.controller;


import com.somoa.serviceback.domain.user.dto.UserLoginDto;
import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@AutoConfigureWebTestClient
@DisplayName("유저 컨트롤러 통합 테스트")
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private UserSignupDto userSignupDto;
    private UserLoginDto userLoginDto;
    private String accessToken;

    private String refreshToken;
    @BeforeEach
    void setUp() {
        userSignupDto = new UserSignupDto("testUser", "password", "testNickname");
        userLoginDto = new UserLoginDto("testUser", "password","testDeviceId","testFcmToken");
    }

    UserSignupDto signUp(UserSignupDto userSignupDto){
        webTestClient.post().uri("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userSignupDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200);
        return userSignupDto;
    }

    void login(UserLoginDto userLoginDto) {
        Map responseBody = webTestClient.post().uri("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userLoginDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class) // 응답 본문을 Map으로 받음
                .returnResult()
                .getResponseBody();

        // 추출된 토큰 저장
        Map<String, String> data = (Map<String, String>) responseBody.get("data");
        this.accessToken = "Bearer " + data.get("accessToken");
        this.refreshToken ="Bearer "  +  data.get("refreshToken");
    }

    @Test
    @DisplayName("[POST] Signup /user/signup")
    void signUpSuccess() {
        // UserSignupDto 객체를 생성하여 signUp 메서드에 전달
        UserSignupDto userSignupDto = new UserSignupDto("testUser", "password", "testNickname");

        // signUp 메서드를 호출하여 사용자 등록을 시도하고, 반환된 DTO 사용
        UserSignupDto registeredUser = signUp(userSignupDto);
    }

    @Test
    @DisplayName("[POST] Login /user/login")
    void loginSuccess() {
        login(userLoginDto);
    }

    @Nested
    @DisplayName("[GET] Auth /user/auth")
    class AuthTest {
        @BeforeEach
        void setup() {
            // 로그인을 수행하고, accessToken 및 refreshToken을 설정
            UserLoginDto loginDto = new UserLoginDto("testUser", "password","testDeviceId","testFcmToken");
            login(loginDto); // 이 메서드가 accessToken과 refreshToken을 설정함
        }

        @Test
        @DisplayName("인증 성공")
        void checkAuthSuccess() {
            webTestClient.get().uri("/user/auth")
                    .header("Authorization", accessToken) // 설정된 accessToken 사용
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200);
        }

        @Test
        @DisplayName("인증 실패 - 잘못된 토큰 사용")
        void checkAuthToBadTokenFail() {
            webTestClient.get().uri("/user/auth")
                    .header("Authorization", "Bearer wrongAccessToken")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("인증 실패 - 리프레시 토큰 사용")
        void checkAuthToRefreshTokenFail() {
            webTestClient.get().uri("/user/auth")
                    .header("Authorization", refreshToken)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("[GET] Refresh /user/refresh")
    class RefreshTokenTest {

        @BeforeEach
        void setup() {
            // 사용자 로그인하여 초기 accessToken 및 refreshToken 설정
            UserLoginDto loginDto = new UserLoginDto("testUser", "password","testDeviceId","testFcmToken");
            login(loginDto);
        }

        @Test
        @DisplayName("토큰 갱신 성공")
        void refreshTokenSuccess() {


            // 리프레시 토큰을 사용하여 토큰 갱신
            Map responseBody = webTestClient.get().uri("/user/refresh")
                    .header("Authorization", refreshToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Map.class)
                    .returnResult()
                    .getResponseBody();

            Map<String, String> data = (Map<String, String>) responseBody.get("data");
            String newAccessToken = "Bearer " + data.get("accessToken");
            String newRefreshToken = "Bearer " + data.get("refreshToken");

            // 새로운 accessToken과 기존 accessToken 비교
            if(!accessToken.equals(newAccessToken)||!refreshToken.equals(newRefreshToken)){
                Assertions.fail("토큰 갱신 실패");
            }

        }
        @Test
        @DisplayName("토큰 갱신 실패- 엑세스토큰이용")
        void refreshTokenToAccessTokenFail() {
            // 엑세스 토큰을 사용하여 토큰 갱신
            webTestClient.get().uri("/user/refresh")
                    .header("Authorization", accessToken)
                    .exchange()
                    .expectStatus().isUnauthorized();

        }
    }
}

