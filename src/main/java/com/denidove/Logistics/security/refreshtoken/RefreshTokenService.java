package com.denidove.Logistics.security.refreshtoken;

import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.exceptions.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    /**
     * TTL refresh-токена.
     * Обычно 14–30 дней.
     */
    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Создание refresh-токена при успешной аутентификации
     * (phone / passwordless / login).
     */
    public RefreshToken create(User user,
                               String userAgent,
                               String ipAddress) {

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(Instant.now().plus(REFRESH_TTL));
        token.setUserAgent(userAgent);
        token.setIpAddress(ipAddress);
        token.setRevoked(false);

        return refreshTokenRepository.save(token);
    }

    /**
     * Валидация refresh-токена
     */
    public RefreshToken validate(String tokenValue) {

        RefreshToken token =  refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid refresh token")
                );

        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return token;
    }

    /**
     * Rotation refresh-токена.
     * Старый токен отзывается, создаётся новый.
     */
    @Transactional
    public RefreshToken rotate(RefreshToken oldToken) {

        oldToken.setRevoked(true);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setUser(oldToken.getUser());
        newToken.setExpiresAt(Instant.now().plus(REFRESH_TTL));
        newToken.setUserAgent(oldToken.getUserAgent());
        newToken.setIpAddress(oldToken.getIpAddress());

        oldToken.setReplacedByToken(newToken.getToken());

        refreshTokenRepository.save(oldToken);
        return  refreshTokenRepository.save(newToken);
    }

    /**
     * Полный logout пользователя
     * (отзыв всех refresh-токенов)
     */
    @Transactional
    public void revokeAll(User user) {

        refreshTokenRepository.findAllByUserAndRevokedFalse(user)
                .forEach(token -> token.setRevoked(true));
    }

    public Optional<RefreshToken> resolveFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("REFRESH_TOKEN"))
                .findFirst()
                .map(Cookie::getValue)
                .flatMap(this::findByValue);
    }

    public ResponseCookie createCookie(RefreshToken token) {
        return ResponseCookie.from("REFRESH_TOKEN", token.getToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict") // или Lax
                .path("/api/auth")
                .maxAge(Duration.between(Instant.now(), token.getExpiresAt())
                        .getSeconds())
                .build();

    }

    public Optional<RefreshToken> findByValue(String value) {
        return refreshTokenRepository.findByToken(value);
    }

}
