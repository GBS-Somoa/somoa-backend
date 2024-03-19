package com.ssdc.serviceback.global.auth;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, org.springframework.security.core.AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        byte[] bytes = "Unauthorized Access - Please check your credentials".getBytes(StandardCharsets.UTF_8);
        // 올바른 DataBuffer 타입 사용
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        // 형변환 없이 직접 Mono<DataBuffer>를 전달
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}