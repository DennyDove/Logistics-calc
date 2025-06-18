package com.denidove.Logistics.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureLogic implements AuthenticationFailureHandler {
    //private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //String email = request.getParameter("email");
        //String error = exception.getMessage();
        System.out.println("A failed login attempt with email!");
        //        + email + ". Reason: " + error);

        String redirectUrl = request.getContextPath() + "/login-1?error";
        response.sendRedirect(redirectUrl);

        //redirectStrategy.sendRedirect(request, response, "/login-1?err");
    }
}
