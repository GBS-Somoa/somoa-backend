package com.somoa.serviceback.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Component
@Service
public class JwtService {


    final private SecretKey accessKey;
    final private SecretKey refreshKey;

    final private JwtParser parser;
    final private JwtParser refreshparser;

    public JwtService(){
        this.accessKey = Keys.hmacShaKeyFor("clzlsvlwkgoaqjrjdusdjdbrghlthrhrltkaruqtkf".getBytes());
        this.refreshKey = Keys.hmacShaKeyFor("rhemddjghldusdjghldbrghlckaclghldbrtktlal".getBytes());
        this.parser = Jwts.parserBuilder().setSigningKey(this.accessKey).build();
        this.refreshparser=Jwts.parserBuilder().setSigningKey(this.refreshKey).build();
    }
    public Mono<Map<String, String>> generateTokens(String userName) {
        return Mono.fromCallable(() -> {
            String accessToken = Jwts.builder()
                    .setSubject(userName)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                    .signWith(this.accessKey)
                    .compact();

            String refreshToken = Jwts.builder()
                    .setSubject(userName)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                    .signWith(this.refreshKey)
                    .compact();

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            return tokens;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public String getUserName(String token) {
        Claims claims = parser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getUserNameFromRefreshToken(String token) {
        Claims claims = refreshparser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public boolean validateAccessToken(String token) {
        try {
            System.out.println("dd");
            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            System.out.println("엑세스토큰 검증실패");
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = refreshparser.parseClaimsJws(token).getBody();
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            System.out.println("리프레시토큰 검증실패");
            return false;
        }
    }


}
