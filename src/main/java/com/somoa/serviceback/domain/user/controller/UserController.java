package com.somoa.serviceback.domain.user.controller;

import com.somoa.serviceback.domain.device.service.DeviceService;
import com.somoa.serviceback.domain.group.dto.GroupRegisterParam;
import com.somoa.serviceback.domain.group.service.GroupManagementService;
import com.somoa.serviceback.domain.user.dto.UserLoginDto;
import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import com.somoa.serviceback.domain.user.service.UserService;
import com.somoa.serviceback.global.annotation.Login;
import com.somoa.serviceback.global.fcm.dto.FcmSendDto;
import com.somoa.serviceback.global.fcm.repository.FcmRepository;
import com.somoa.serviceback.global.fcm.service.FcmService;
import com.somoa.serviceback.global.handler.ResponseHandler;
import com.somoa.serviceback.global.auth.AuthConverter;
import com.somoa.serviceback.global.auth.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    final ReactiveUserDetailsService users;
    final JwtService jwtService;
    final AuthConverter authConverter;
    final FcmService fcmService;
    final GroupManagementService groupManagementService;
    private final FcmRepository fcmRepository;

    @GetMapping("/refresh")
    public Mono<ResponseEntity<ResponseHandler>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            return userService.refreshAccessToken(refreshToken)
                    .flatMap(tokens -> ResponseHandler.ok(tokens, "토큰이 정상적으로 갱신되었습니다."))
                    .onErrorResume(e -> {
                        System.out.println(e);
                        return ResponseHandler.error("유효하지 않거나 기간이 지난 토큰입니다.", HttpStatus.UNAUTHORIZED); // 401 Unauthorized
                    });
        } else {
            return ResponseHandler.error("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ResponseHandler>> login(@RequestBody UserLoginDto user) {
        return userService.loginUser(user.getUsername(), user.getPassword())
                .flatMap(response -> {
                    String userId = response.get("userId").toString();
                    String nickname = response.get("nickname").toString();
                    Map<String, String> data = (Map<String, String>) response.get("tokens");
                    data.put("nickname", nickname);
                    return fcmService.saveOrUpdateFcmToken(Integer.parseInt(userId), user.getMobileDeviceId(), user.getFcmToken())
                            .then(ResponseHandler.ok(data, "로그인 성공")); // 변경된 부분
                })
                .onErrorResume(e -> {
                    if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                        return ResponseHandler.error("유효하지 않은 사용자 이름 또는 비밀번호", HttpStatus.UNAUTHORIZED);
                    } else {
                        log.warn("login error: ", e);
                        return ResponseHandler.error("서버 오류로 인해 로그인을 처리할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

    @GetMapping("/auth")
    public Mono<ResponseEntity<ResponseHandler>> auth() {
        return ResponseHandler.ok("인증성공", "인증이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<ResponseHandler>> signUp(@RequestBody UserSignupDto userSignupDto) {
        return userService.signUp(userSignupDto)
                .flatMap(data -> {
                    Map<String, Object> responseData = (Map<String, Object>) data;
                    Integer userId = (Integer) responseData.get("id");
                    GroupRegisterParam groupRegisterParam = new GroupRegisterParam();
                    groupRegisterParam.setGroupName("우리집");
                    return groupManagementService.save(userId, groupRegisterParam)
                            .flatMap(data2 -> ResponseHandler.ok(responseData, "회원가입이 성공적으로 완료되었습니다."));
                })
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return ResponseHandler.error("에러 발생", HttpStatus.BAD_REQUEST);
                    }
                    return ResponseHandler.error("에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping("/check")
    public Mono<ResponseEntity<ResponseHandler>> checkAuth() {
        System.out.println("인증완료");
        return ResponseHandler.ok("hello", "인증된 유저");

    }

    @GetMapping("/orders")
    public Mono<ResponseEntity<ResponseHandler>> getOrders(@Login Integer loginUserId) {
        return userService.getOrders(loginUserId)
                        .flatMap(responseData -> ResponseHandler.ok(responseData, "주문조회가 완료되었습니다.."));
    }

    @GetMapping("/sendNotifications")
    public Mono<ResponseEntity<String>> sendNotificationsToAllUsers() {
        return fcmRepository.findAll()
                .flatMap(fcmToken -> {
                    FcmSendDto fcmSendDto = new FcmSendDto(fcmToken.getToken(), "제목", "본문","아이콘","경로","데이터",1);
                    return fcmService.sendMessageTo(fcmSendDto);
                })
                .collectList()
                .flatMap(resultList -> {
                    long successCount = resultList.stream().filter(result -> result == 1).count();
                    if (successCount > 0) {
                        return Mono.just(ResponseEntity.ok().body(successCount + " messages sent successfully"));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send messages"));
                    }
                });
    }
}
