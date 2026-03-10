package com.denidove.Logistics.security.jwt;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.security.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ Логируем факт вызова фильтра и URI
        System.out.println(">>> JwtAuthFilter start for URI: " + request.getRequestURI());


        if (request.getRequestURI().startsWith("/login-main")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1️⃣ Проверяем, есть ли в контексте уже аутентифицированный пользователь
        // (например, если контекст уже был установлен где-то ранее)
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated() && !(existing instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }


        // 2️⃣ Пытаемся достать JWT-токен из cookie
        String jwt = extractJwtFromCookie(request);

        // Если токен отсутствует — просто продолжаем фильтрацию дальше (анонимный пользователь)
        // Этот сценарий например нужен для доступа неаутентифицированного пользователя на "/"
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Извлекаем из токена userId (subject)
        Long userId = null;
        try {
            userId = jwtService.extractUserId(jwt);
        } catch (JwtException j) {
            // Удаляем cookie с JWT
            // Если cookie не удалить, тогда при переходе на страницу "/" с "протухшим" jwt-токеном
            // может получиться непредсказуемое поведение браузера. Например циклический редирект
            jwtService.clearJwtCookie(response);
        }

        // Если имя пользователя не найдено — токен недействителен
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4️⃣ Загружаем данные пользователя из БД через UserDetailsService

        UserDetails userDetails = userDetailsService.loadUserById(userId);

        System.out.println("JWT userId: " + userId);
        System.out.println("UserDetails username: " + ((SecurityUser) userDetails).getLogin());

        // 5️⃣ Проверяем, что токен действительно принадлежит этому пользователю и не истёк
        if (jwtService.validateToken(jwt, userDetails)) {

            // ✅ Формируем объект Authentication, который Spring Security будет считать "вошедшим пользователем"
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,   // principal — данные пользователя
                            null,          // credentials (пароль не нужен)
                            userDetails.getAuthorities() // роли и права
                    );

            // 6️⃣ Устанавливаем контекст безопасности вручную
            //SecurityContextHolder.getContext().setAuthentication(authToken); // допустимый вариант

            //Наиболее надёжный и чистый по архитектуре вариант:
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            System.out.println("JWT filter executed for URI: " + request.getRequestURI());
        }

        // 7️⃣ Передаём запрос дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Вспомогательный метод — достаёт JWT-токен из cookies звпроса.
     * Можно легко заменить на заголовок Authorization, если понадобится.
     */
    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /* Второй вариант - более читабельный для новичков
    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }*/
}
