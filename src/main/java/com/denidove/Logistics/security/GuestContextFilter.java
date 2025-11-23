package com.denidove.Logistics.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class GuestContextFilter extends OncePerRequestFilter {

    private final String COOKIE_NAME = "guest_id";
    private final String REDIS_KEY_PREFIX = "guest:";
    private final StringRedisTemplate redisTemplate;

    public GuestContextFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String guestId = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (COOKIE_NAME.equals(c.getName())) {
                    guestId = c.getValue();
                    break;
                }
            }
        }

        if (guestId == null) {
            guestId = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, guestId)
                    .path("/")
                    .httpOnly(false)
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
        }

        String redisKey = REDIS_KEY_PREFIX + guestId;
        // Для максимально «защищённого» кода от NPE — Boolean.FALSE
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.opsForHash().put(redisKey, "createdAt", Instant.now().toString());
            redisTemplate.expire(redisKey, Duration.ofDays(7));
        }

        request.setAttribute("guestId", guestId);
        filterChain.doFilter(request, response);
    }
}

