package com.somoa.serviceback.global.auth;

import com.somoa.serviceback.global.auth.dto.UserInfo;
import com.somoa.serviceback.global.config.PropertiesConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

@Slf4j
@Component
@Service
public class JwtService {

    private final PropertiesConfig propertiesConfig;

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    private final JwtParser parser;
    private final JwtParser refreshparser;

    public JwtService(PropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
        this.accessKey = Keys.hmacShaKeyFor(propertiesConfig.getAccessKey().getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(propertiesConfig.getRefreshKey().getBytes());
        this.parser = Jwts.parserBuilder().setSigningKey(this.accessKey).build();
        this.refreshparser = Jwts.parserBuilder().setSigningKey(this.refreshKey).build();
    }

    public Mono<Map<String, String>> generateTokens(UserInfo userInfo) {
        return Mono.fromCallable(() -> {
            String accessToken = Jwts.builder()
                    .setSubject(userInfo.getUsername())
                    .claim("id", userInfo.getId())
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(3, ChronoUnit.HOURS)))
                    .signWith(this.accessKey)
                    .compact();

            String refreshToken = Jwts.builder()
                    .setSubject(userInfo.getUsername())
                    .claim("id", userInfo.getId())
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

    public Object getUserId(String token) {
        Claims claims = parser.parseClaimsJws(token).getBody();
        return claims.get("id");
    }

    public UserInfo getUserInfoFromRefreshToken(String refreshToken) {
        Claims claims = refreshparser.parseClaimsJws(refreshToken).getBody();
        return new UserInfo((Integer) claims.get("id"), claims.getSubject());
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.info("엑세스 토큰 검증 실패");
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = refreshparser.parseClaimsJws(token).getBody();
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.info("리프레시 토큰 검증 실패");
            return false;
        }
    }
}
