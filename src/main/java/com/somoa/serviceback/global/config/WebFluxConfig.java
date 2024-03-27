package com.somoa.serviceback.global.config;

import com.somoa.serviceback.global.argumentresolver.LoginUserArgumentResolver;
import com.somoa.serviceback.global.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebFluxConfig implements WebFluxConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new LoginUserArgumentResolver(jwtService));
    }
}
