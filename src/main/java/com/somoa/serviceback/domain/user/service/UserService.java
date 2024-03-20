package com.somoa.serviceback.domain.user.service;

import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import com.somoa.serviceback.domain.user.entity.User;
import com.somoa.serviceback.domain.user.repository.UserRepository;
import com.somoa.serviceback.global.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReactiveUserDetailsService users;
    private final JwtService jwtService;



    public Mono<Map<String, String>> loginUser(String username, String password) {
        return users.findByUsername(username)
                .flatMap(userDetails -> {

                    if (password.equals(userDetails.getPassword())) {
                        // 비밀번호가 일치하는 경우, 토큰 생성
                        return jwtService.generateTokens(userDetails.getUsername());
                    } else {
                        // 비밀번호가 일치하지 않는 경우, 에러 반환
                        return Mono.error(new IllegalArgumentException("Invalid username or password"));
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found"))); // 사용자가 없는 경우 에러 반환
    }

    public Mono<Map<String, String>> refreshAccessToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            return Mono.error(new IllegalArgumentException("Invalid or expired refresh token"));
        }

        return Mono.just(refreshToken)
                .flatMap(token -> Mono.justOrEmpty(jwtService.getUserNameFromRefreshToken(token)))
                .flatMap(username -> {
                    if (username == null || username.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Token does not contain a valid username"));
                    }
                    return jwtService.generateTokens(username); // 비동기 메소드 호출로 변경
                });
    }

    public Mono<Object> signUp(UserSignupDto userSignUpDto) {
        return userRepository.findByUsername(userSignUpDto.getUsername())
                .flatMap(existingUser -> Mono.error(new IllegalArgumentException("이미 존재하는 사용자 이름입니다.")))
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = User.builder()
                            .username(userSignUpDto.getUsername())
                            .password(userSignUpDto.getPassword())
                            .nickname(userSignUpDto.getNickname())
                            .build();
                    return userRepository.save(newUser)
                            .map(savedUser -> {
                                Map<String, Integer> responseContent = new HashMap<>();
                                responseContent.put("id", savedUser.getId());
                                return responseContent;
                            });
                }));
    }
}