package com.ssdc.serviceback.global.auth;

import com.ssdc.serviceback.domain.user.repository.UserRepository;
import com.ssdc.serviceback.global.handler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Mono;


@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
    private final UserRepository userRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,AuthConverter jwtAuthConverter,AuthManager jwtAuthManager ) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthManager);
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
                        .pathMatchers("/user/login", "/user/refresh","/user/signup").permitAll()
                        .pathMatchers("/**").authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인을 비활성화
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(authenticationManager -> authenticationManager
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }


    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        System.out.println("ddasd");
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_USER")
                        .build())
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("사용자를 찾을 수 없습니다.")));
    }


}
