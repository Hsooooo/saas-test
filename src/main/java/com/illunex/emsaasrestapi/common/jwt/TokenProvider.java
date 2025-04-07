package com.illunex.emsaasrestapi.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Process a token for JWT
 */
@Slf4j
@Component
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String INVALID_JWT_TOKEN = "Invalid JWT token";

    private final java.security.Key key; // due to security and performance
    private final JwtParser jwtParser;
    private final int accessExpireSeconds;
    private final int refreshExpireSeconds;

    @Autowired
    public TokenProvider(@Value("${jwt.secret}") String secretKey,
                         @Value("${jwt.access-token-expiration:1800}") int accessExpireSeconds,
                         @Value("${jwt.refresh-token-expiration:86400}") int refreshExpireSeconds) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(Objects.requireNonNull(secretKey, "`jwt.secret` must be required")));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        this.accessExpireSeconds = 1000 * accessExpireSeconds;
        this.refreshExpireSeconds = 1000 * refreshExpireSeconds;
    }

    // 엑세스 토큰 생성
    public String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpireSeconds))
                .compact();
    }

    // 리프레쉬 토큰 생성
    public String generateRefreshToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpireSeconds))
                .compact();
    }

    public Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(refreshExpireSeconds); // 12h
        cookie.setHttpOnly(true);   //JS로 접근 불가, 탈취 위험 감소
        return cookie;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .filter(auth -> !auth.trim().isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);
        return UsernamePasswordAuthenticationToken.authenticated(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            log.trace(INVALID_JWT_TOKEN, e);
        } catch (IllegalArgumentException e) {
            log.error("Token validation error {}", e.getMessage());
        }
        return false;
    }

    @Deprecated // deprecated due to performance issues
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 유저 정보 가져오기
    public String getEmailFromToken(String token) {
        if(token.startsWith("Bearer ")){
            token = token.replace("Bearer ", "");
        }
        return getClaimFromToken(token);
    }

    public String getClaimFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }
}
