package com.denidove.Logistics.security.phone;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.security.jwt.JwtService;
import com.denidove.Logistics.security.refreshtoken.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
public class PhoneAuthHandlerConfig {

    private final JwtService jwtService;
    private final RefreshTokenService refreshService;

    @Bean
    public AuthenticationSuccessHandler phoneAuthSuccessHandler() {
        return (req, resp, auth) -> {
            SecurityUser user = (SecurityUser) auth.getPrincipal();
            Long userId = user.getId();

            String jwt = jwtService.generateToken(userId);
            jwtService.addJwtCookie(resp, jwt);

            refreshService.create(user.getUser(),
                    req.getHeader("User-Agent"),
                    req.getRemoteAddr());

            //resp.sendRedirect("/");
        };
    }

    @Bean
    public AuthenticationFailureHandler phoneAuthFailureHandler() {
        return (req, resp, ex) -> {
            System.out.println("🔥 PHONE FAILURE HANDLER CALLED");

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("""
                {
                  "error": "INVALID_CODE"
                }
                """);
            //resp.sendRedirect("/login-main?error");
        };
    }
}
