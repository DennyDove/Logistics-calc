package com.denidove.Logistics.security;

import com.denidove.Logistics.exceptions.VerificationCodeErrorException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureLogic implements AuthenticationFailureHandler {
    //private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFailureLogic.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        String errorParam = "bad_credentials"; // значение по умолчанию
        if (exception instanceof VerificationCodeErrorException) {
            errorParam = "invalid_code";
        }

        log.warn("Ошибка аутентификации: {}", exception.getMessage());

        response.sendRedirect(request.getContextPath() + "/login-1?error=" + errorParam);
    }

        //response.sendRedirect(context + "/login-1?error=" + errorParam);
        //redirectStrategy.sendRedirect(request, response, "/login-1?error=" + errorParam);

        /* Было:
        //String email = request.getParameter("email");
        //String error = exception.getMessage();
        System.out.println("A failed login attempt with email!");

        String redirectUrl = request.getContextPath() + "/login-1?error";
        response.sendRedirect(redirectUrl);
    }
    */

}
