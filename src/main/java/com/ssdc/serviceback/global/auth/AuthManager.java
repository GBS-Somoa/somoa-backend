package com.ssdc.serviceback.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
@Component
@RequiredArgsConstructor
public class AuthManager implements ReactiveAuthenticationManager {
    final JwtService jwtService;

    final ReactiveUserDetailsService users;



    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .cast(BearerToken.class)
                .flatMap(auth -> {
                    System.out.println("검사");
                    String token = auth.getCredentials();
                    String username = jwtService.getUserName(token); // 액세스 토큰에서 사용자 이름 추출
                    // 액세스 토큰 유효성 검사
                    if (jwtService.validateAccessToken(token)) {
                        return processAuthentication(username);
                    }// 새로운 액세스 토큰으로 인증 처리
                    else {
                        return Mono.error(new IllegalArgumentException("Invalid or expired token"));
                    }
                });
    }

    private Mono<Authentication> processAuthentication(String username) {
        return users.findByUsername(username)
                .flatMap(userDetails -> Mono.just(new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                )));
    }
}
