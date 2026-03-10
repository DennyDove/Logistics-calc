package com.denidove.Logistics.security.jwt;

import com.denidove.Logistics.entities.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${spring.jwt.secret}")
    private String SECRET;

    @Value("${spring.jwt.expiration}")
    private long EXPIRATION_MS; // 60 мин. 23000 23 секунды

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }


    @Value("${spring.jwt.cookie-name:jwt}")
    private String cookieName;


    // --------------------------------------------------------------------
    // 1. Создание JWT
    // --------------------------------------------------------------------
    public String generateToken(Long userId) {

        Instant now = Instant.now();
        Instant exp = now.plusMillis(EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))       // OK
                .setIssuedAt(Date.from(now))              // OK
                .setExpiration(Date.from(now.plusMillis(EXPIRATION_MS))) // OK
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    // --------------------------------------------------------------------
    // 6. Проверка валидности токена (использует UserDetails)
    // --------------------------------------------------------------------
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            String userIdentifier;
            if (userDetails instanceof SecurityUser securityUser) {
                userIdentifier = securityUser.getId().toString(); // ✅ достаём id, если доступен
            } else {
                userIdentifier = userDetails.getUsername();  // fallback
            }

            // ✅ Проверка:
            // 1) имя пользователя в токене совпадает с реальным пользователем
            // 2) токен не истёк
            return username.equals(userIdentifier)
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // --------------------------------------------------------------------
    // 7. Создание cookie c JWT
    // --------------------------------------------------------------------
    public void addJwtCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(EXPIRATION_MS / 1000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // --------------------------------------------------------------------
    // 8. Удаление JWT cookie (на logout/invalid token)
    // --------------------------------------------------------------------
    public void clearJwtCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public long getEXPIRATION_MS() {
        return EXPIRATION_MS;
    }

}
