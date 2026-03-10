package com.denidove.Logistics.security.phone;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

public class PhoneAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public PhoneAuthenticationFilter(AuthenticationManager authenticationManager) {
        // Сточка ниже означает, что фильтр сработает только на запрос /api/auth/phone-login
        super("/api/auth/phone-login"); // loginProcessingUrl
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {

        String phone = request.getParameter("phone");
        String code = request.getParameter("code");

        PhoneAuthenticationToken token = new PhoneAuthenticationToken(phone, code);

        return this.getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {

        // КЛЮЧЕВОЙ МОМЕНТ
        getFailureHandler()
                .onAuthenticationFailure(request, response, failed);
    }
}

