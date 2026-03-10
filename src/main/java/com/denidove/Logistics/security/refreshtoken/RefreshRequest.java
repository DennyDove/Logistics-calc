package com.denidove.Logistics.security.refreshtoken;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

// Данный класс это DTO входящего запроса для эндпоинта /api/auth/refresh
// Используется в реализации refresh-лигики без Cookies --> Mobile / SPA / чистый API
public class RefreshRequest {
    private String refreshToken;
}
