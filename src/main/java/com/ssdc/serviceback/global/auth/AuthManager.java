package com.ssdc.serviceback.global.auth;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

@SuppressWarnings("ClassCanBeRecord")
@Component
public class AuthManager implements ReactiveAuthenticationManager {
    final JwtService jwtService;

    final ReactiveUserDetailsService users;


    public AuthManager(JwtService jweService, ReactiveUserDetailsService users) {
        this.jwtService = jweService;
        this.users = users;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication){
        return Mono.justOrEmpty(
                authentication
        )
                .cast(BearerToken.class)
                .flatMap(auth -> {
                    String getUsername = jwtService.getUserName(auth.getCredentials());
                    Mono<UserDetails> foundUser = users.findByUsername(getUsername).defaultIfEmpty(new UserDetails() {
                        @Override
                        public Collection<? extends GrantedAuthority> getAuthorities() {
                            return null;
                        }

                        @Override
                        public String getPassword() {
                            return null;
                        }

                        @Override
                        public String getUsername() {
                            return null;
                        }

                        @Override
                        public boolean isAccountNonExpired() {
                            return false;
                        }

                        @Override
                        public boolean isAccountNonLocked() {
                            return false;
                        }

                        @Override
                        public boolean isCredentialsNonExpired() {
                            return false;
                        }

                        @Override
                        public boolean isEnabled() {
                            return false;
                        }
                    });

                    Mono<Authentication> authenticatedUser = foundUser.flatMap(u ->{
                        if(u.getUsername()==null){
                            Mono.error(new IllegalArgumentException("유저가 존재하지 않습니다."));
                        }
                        if(jwtService.validate(u,auth.getCredentials())){
                            return Mono.justOrEmpty(new UsernamePasswordAuthenticationToken(u.getUsername(),u.getPassword(),u.getAuthorities()));
                        }
                        Mono.error(new IllegalArgumentException("유효하지 않거나 만료된 토큰"));
                        return Mono.justOrEmpty(new UsernamePasswordAuthenticationToken(u.getUsername(),u.getPassword(),u.getAuthorities()));
                    });

                    return authenticatedUser;
                });

    }
}
