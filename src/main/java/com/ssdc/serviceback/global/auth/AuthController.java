package com.ssdc.serviceback.global.auth;

import com.ssdc.serviceback.domain.user.dto.UserInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RestController
public class AuthController {

    final ReactiveUserDetailsService users;
    final JwtService jwtService;

    final PasswordEncoder encoder;
    public AuthController(ReactiveUserDetailsService users, JwtService jwtService, PasswordEncoder encoder){
        this.users = users;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    @GetMapping("/auth")
    public Mono<ResponseEntity<ReqRespModel<String>>> auth(){
        return Mono.just(
                ResponseEntity.ok(
                        new ReqRespModel<>("하이","")
                )
        );
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ReqRespModel<String>>> login(@RequestBody ReqLogin user){
        Mono<UserDetails> foundUser = users.findByUsername(user.getName()).defaultIfEmpty(new UserDetails() {
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

        return foundUser.map(
                u->{
                    if(u.getUsername()==null){
                        return ResponseEntity.status(404).body(new ReqRespModel<>("","유저를 찾을 수 없습니다."));
                    }
                    if(encoder.matches(user.getPassword(),u.getPassword())){
                        return ResponseEntity.ok(new ReqRespModel<>(jwtService.generate(u.getUsername()),"성공"));
                    }
                    return ResponseEntity.badRequest().body(new ReqRespModel<>("","유효하지 않은 비밀번호"));
                }
        );

        /**
        return foundUser.flatMap(u ->{
            if(u!=null){
                // 비밀번호 맞는지 검사
                if(encoder.matches(user.getPassword(),u.getPassword())){
                    return Mono.just(
                            ResponseEntity.ok(
                                    new ReqRespModel<>(jwtService.generate(u.getUsername()),"Success")
                            )
                    );
                }
            return Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqRespModel<>("","유효하지 않은 인증"))
            );
            }
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ReqRespModel<>("","없는 아이디입니다. 회원가입해주세요.")));
        });
         */
    }
}
