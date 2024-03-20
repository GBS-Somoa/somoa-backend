package com.somoa.serviceback.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
                    String token = auth.getCredentials();
                    if (jwtService.validateAccessToken(token)) {
                        // 액세스 토큰 유효성 검사
                        String username = jwtService.getUserName(token);
                        return processAuthentication(username);
                    }
                    else {// 유효기간 지났거나, 토큰이아닌놈
                        return Mono.error(new AuthenticationException("Invalid or expired token") {
                        });
                    }
                });
    }

    private Mono<Authentication> processAuthentication(String username) {
        return users.findByUsername(username)
                .flatMap(userDetails -> {
                    System.out.println(userDetails);
                    return Mono.just(new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                ));});
    }
}
