package com.somoa.serviceback.domain.device.controller;


import com.somoa.serviceback.domain.user.dto.UserLoginDto;
import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import com.somoa.serviceback.global.auth.SecurityConfigTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(DeviceController.class)
@Import(SecurityConfigTest.class)
public class DeviceControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    private UserLoginDto userLoginDto;
    private String accessToken;

    private String refreshToken;

    @Nested
    @DisplayName("[GET] Auth /user/auth")
    class AuthTest {
        @BeforeEach
        void setUp() {
            userLoginDto = new UserLoginDto("testUser", "password", "testDeviceId", "testFcmToken");
        }

        @Test
        @DisplayName("[POST] 기기상태정보 받음")
        void postDeviceStatus() {
            String requestBody = "\n" +
                    "{" +
                    "    deviceModel\": \"BESPOKE 큐브 Air Infinite Line\"," +
                    "    \"deviceManufacturer\": \"SAMSUNG\",\n" +
                    "    \"deviceType\": \"airPurifier\",\n" +
                    "    \"deviceId\": \"daeeffe8\",\n" +
                    "    \"supplies\": [\n" +
                    "        {\n" +
                    "            \"supplyType\": \"replaceableFilter\",\n" +
                    "            \"supplyName\": \"공기청정기 좌측 필터(교체형)\",\n" +
                    "            \"dataProvided\": [\n" +
                    "                \"supplyStatus\",\n" +
                    "                \"supplyChangeDate\"\n" +
                    "            ],\n" +
                    "            \"values\": [\n" +
                    "                null,\n" +
                    "                null\n" +
                    "            ]\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"supplyType\": \"replaceableFilter\",\n" +
                    "            \"supplyName\": \"공기청정기 우측 필터(교체형)\",\n" +
                    "            \"dataProvided\": [\n" +
                    "                \"supplyStatus\",\n" +
                    "                \"supplyChangeDate\"\n" +
                    "            ],\n" +
                    "            \"values\": [\n" +
                    "                null,\n" +
                    "                null\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    ],\n" +
                    "}"; // 요청 본문 생략
            webTestClient.post().uri("/device/daeeffe8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200);

        }

    }
}


