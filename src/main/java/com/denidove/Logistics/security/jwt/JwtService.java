package com.denidove.Logistics.security.jwt;

import com.denidove.Logistics.entities.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

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

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

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
                userIdentifier = securityUser.getLogin(); // ✅ достаём логин, если доступен
            } else {
                userIdentifier = userDetails.getUsername();
            }

            // ✅ Проверка:
            // 1) имя пользователя в токене совпадает с реальным пользователем
            // 2) токен не истёк
            return username.equals(userIdentifier)
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            // Удаляем cookie с JWT. Перенес это в JwtAuthenticationFilter
            // Если cookie не удалить, тогда при переходе на страницу "/" с "протухшим" jwt-токеном
            // может получиться непредсказуемое поведение браузера. Например циклический редирект
            /*
            ResponseCookie cookie = ResponseCookie.from("jwt", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString()); */
            return false;
        }
    }
}
