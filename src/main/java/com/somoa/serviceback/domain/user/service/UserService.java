package com.somoa.serviceback.domain.user.service;

import com.somoa.serviceback.domain.device.repository.DeviceRepository;
import com.somoa.serviceback.domain.order.dto.OrderWithGroupnameResponse;
import com.somoa.serviceback.domain.order.repository.OrderRepository;
import com.somoa.serviceback.domain.user.dto.UserSignupDto;
import com.somoa.serviceback.domain.user.entity.User;
import com.somoa.serviceback.domain.user.repository.UserRepository;
import com.somoa.serviceback.global.auth.JwtService;
import com.somoa.serviceback.global.auth.dto.UserInfo;
import com.somoa.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final OrderRepository orderRepository;
    private final DeviceRepository deviceRepository;

    public Mono<Map<String, Object>> loginUser(String username, String password) {
        // UserRepository를 사용하여 사용자 인증 진행
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (password.equals(user.getPassword())) {
                        // 비밀번호가 일치하는 경우, 토큰 생성
                        UserInfo userInfo = UserInfo.of(user);
                        return jwtService.generateTokens(userInfo)
                                .map(tokens -> {
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("tokens", tokens);
                                    response.put("userId", user.getId());
                                    response.put("nickname", user.getNickname());
                                    return response;
                                });
                    } else {
                        // 비밀번호가 일치하지 않는 경우, 에러 반환
                        return Mono.error(new BadCredentialsException("Invalid username or password"));
                    }
                })
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")));
    }
    public Mono<Map<String, String>> refreshAccessToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            return Mono.error(new IllegalArgumentException("Invalid or expired refresh token"));
        }

        return Mono.just(refreshToken)
                .flatMap(token -> Mono.justOrEmpty(jwtService.getUserInfoFromRefreshToken(token)))
                .flatMap(userInfo -> {
                    if (userInfo == null || userInfo.getUsername().isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Token does not contain a valid username"));
                    }
                    return jwtService.generateTokens(userInfo); // 비동기 메소드 호출로 변경
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

    @Transactional(readOnly=true)
    public Mono<List<OrderWithGroupnameResponse>> getOrders(Integer loginUserId) {
        return orderRepository.findByUserIdWithGroupName(loginUserId)
                .flatMap(orderResponse -> {
                    return deviceRepository.findFirstDeviceBySupplyId(orderResponse.getSupplyId())
                            .map(device -> {
                                orderResponse.setDeviceId(device.getId());
                                orderResponse.setDeviceName(device.getNickname());
                                return orderResponse;
                            })
                            .defaultIfEmpty(orderResponse); // Device 정보가 없는 경우, 기존 OrderWithGroupnameResponse 객체 반환
                })
                .collectList();
    }
}