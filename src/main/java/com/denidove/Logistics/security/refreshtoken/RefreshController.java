package com.denidove.Logistics.security.refreshtoken;

import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class RefreshController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. Достаём refresh token из cookie
        RefreshToken refreshToken = refreshTokenService
                .resolveFromCookie(request)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Refresh token not found"
                        )
                );

        // 2. Валидируем refresh token
        RefreshToken oldToken = refreshTokenService.validate(refreshToken.getToken());

        // 3. Достаем User из токена
        User user = refreshToken.getUser();

        // 3. Выпускаем новый jwt-токен (access token)
        String newAccessToken = jwtService.generateToken(user.getId());

        // 4. Ротация refresh token (invalidate старый + создать новый)
        RefreshToken newRefreshToken =
                refreshTokenService.rotate(refreshToken);

        // 5. Положить новый refresh token в cookie
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenService.createCookie(newRefreshToken).toString()
        );

        // 6. Вернуть Jwt-токен (т.е. access-токен) клиенту
        return ResponseEntity.ok(
                new TokenResponse(
                        newAccessToken,
                        null, // refresh НЕ возвращаем в body
                        jwtService.getEXPIRATION_MS()  // == getAccessTtlSeconds()
                )
        );
    }



    /**
    @PostMapping("/api/auth/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {  //--> вариант контроллера refresh для Mobile / SPA / чистый API

        // 1. Проверка валидности refresh-токена
        RefreshToken oldToken =
                refreshTokenService.validate(request.getRefreshToken());

        // 2. Rotation
        RefreshToken newToken =
                refreshTokenService.rotate(oldToken);

        // 3. Новый access-token
        String newAccess =
                jwtService.issue(oldToken.getUser());

        return new TokenResponse(
                newAccess,
                newToken.getToken()
        );
    }*/


}
