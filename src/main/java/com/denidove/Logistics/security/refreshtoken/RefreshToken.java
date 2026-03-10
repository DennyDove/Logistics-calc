package com.denidove.Logistics.security.refreshtoken;

import com.denidove.Logistics.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сам refresh-токен.
     * ❗ НЕ JWT, а случайная строка (UUID).
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * Пользователь, которому принадлежит refresh-токен
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    /**
     * Дата истечения
     */
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Отозван ли токен (logout, security event)
     */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Для rotation / reuse-detection
     */
    @Column(length = 64)
    private String replacedByToken;

    /**
     * Контекст устройства (опционально, но полезно)
     */
    private String userAgent;
    private String ipAddress;

}





