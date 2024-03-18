package com.ssdc.serviceback.global.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;


@Configuration
//@RequiredArgsConstructor
@EnableWebFluxSecurity
//@Slf4j
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,AuthConverter jwtAuthConverter,AuthManager jwtAuthManager) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
        jwtFilter.setServerAuthenticationConverter(jwtAuthConverter);

        // /user/login과 /user/refresh를 제외한 모든 /api/** 경로에 jwtFilter 적용
        OrServerWebExchangeMatcher pathsToExclude = new OrServerWebExchangeMatcher(
                new PathPatternParserServerWebExchangeMatcher("/user/login"),
                new PathPatternParserServerWebExchangeMatcher("/user/refresh")
        );
        NegatedServerWebExchangeMatcher pathsToInclude = new NegatedServerWebExchangeMatcher(pathsToExclude);

        // Custom matcher를 jwtFilter에 적용
        jwtFilter.setRequiresAuthenticationMatcher(pathsToInclude);

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/user/login", "/user/refresh").permitAll()
                        .pathMatchers("/api/**").authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인을 비활성화
                .csrf(csrf -> csrf.disable())
                .build(); // CSRF 보호를 비활성화
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User
                .withUsername("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

}
