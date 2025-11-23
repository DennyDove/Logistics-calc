package com.denidove.Logistics.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException)
            throws IOException {

        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");

        //Чтобы селать поведение ещё более устойчивым, можно добавить "страховочную" проверку на response.isCommitted()
        //(на случай, если какой-то фильтр уже начал ответ):
        if (response.isCommitted()) {
            return;
        }

        /* Данный блок убираем, т.к. раньше была единая цепочка, и этот кусок был полезен — он не позволял редиректить браузер на /login,
        если кто-то случайно запрашивал CSS без токена. Теперь же, после введения staticResourcesChain, запросы к статике не вызывают 401/403,
        а просто разрешаются. Следовательно, данный блок никогда не сработает:
        // 🔹 1. Игнорируем запросы к статике
        if (uri.startsWith("/js/") || uri.startsWith("/styles/") || uri.startsWith("/images/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }*/

        // 🔹 2. API или AJAX — возвращаем JSON с 401
        if ((accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
                || uri.startsWith("/api/")) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized or expired token\"}");
            return;
        }

        // 🔹 3. HTML-запросы — редирект на страницу входа
        response.sendRedirect("/login-1");
    }
}
