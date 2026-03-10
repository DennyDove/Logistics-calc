package com.denidove.Logistics.security.phone;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PhoneAuthenticationConfig {

    private final PhoneAuthenticationProvider phoneAuthProvider;

    @Bean
    public AuthenticationManager phoneAuthManager() {
        return new ProviderManager(List.of(phoneAuthProvider));
    }

    @Bean
    public PhoneAuthenticationFilter phoneAuthenticationFilter(
            AuthenticationManager phoneAuthManager,
            AuthenticationSuccessHandler phoneAuthSuccessHandler,
            @Qualifier("phoneAuthFailureHandler") // это сделано, так у нас два AuthFailureHandler, чтобы Spring выбирал правильный
            AuthenticationFailureHandler phoneAuthFailureHandler
    ) {

        PhoneAuthenticationFilter filter =
                new PhoneAuthenticationFilter(phoneAuthManager);

        filter.setAuthenticationSuccessHandler(phoneAuthSuccessHandler);
        filter.setAuthenticationFailureHandler(phoneAuthFailureHandler);

        return filter;
    }
}
