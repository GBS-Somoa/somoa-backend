package com.somoa.serviceback.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.somoa.serviceback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;


@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
    private final UserRepository userRepository;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 자격 증명(쿠키, HTTP 인증 등)을 허용
        // 모든 출처를 허용하는 대신 패턴 사용
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.setAllowCredentials(true); // 쿠키, 인증과 관련된 헤더 등을 허용
        //config.setMaxAge(3600L); // pre-flight 요청의 최대 캐시 시간 (초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 해당 설정 적용
        return source;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,AuthConverter jwtAuthConverter,AuthManager jwtAuthManager ) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
        jwtFilter.setAuthenticationFailureHandler(new CustomAuthenticationFailureHandler(new ObjectMapper()));
        jwtFilter.setServerAuthenticationConverter(jwtAuthConverter);
        // 아래경로를 제외하고 모든 경로에 jwtFilter 적용

        OrServerWebExchangeMatcher pathsToExclude = new OrServerWebExchangeMatcher(
                new PathPatternParserServerWebExchangeMatcher("/user/login"),
                new PathPatternParserServerWebExchangeMatcher("/user/refresh"),
                new PathPatternParserServerWebExchangeMatcher("/user/signup")
                );
        NegatedServerWebExchangeMatcher pathsToInclude = new NegatedServerWebExchangeMatcher(pathsToExclude);

        // Custom matcher를 jwtFilter에 적용
        jwtFilter.setRequiresAuthenticationMatcher(pathsToInclude);

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(HttpMethod.POST, "/orders").permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/orders/{order_store}/{order_id}").permitAll()
                        .pathMatchers(HttpMethod.POST,"/devices/{device_id}").permitAll()
                        .pathMatchers("/user/login", "/user/refresh","/user/signup").permitAll()
                        .pathMatchers("/test").permitAll()
                        .pathMatchers("/**").authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인을 비활성화
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(authenticationManager -> authenticationManager
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(new ObjectMapper())))

                .build();
    }


    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_USER")
                        .build())
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("사용자를 찾을 수 없습니다.")));
    }


}
