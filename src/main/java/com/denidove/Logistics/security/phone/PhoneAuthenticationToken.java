package com.denidove.Logistics.security.phone;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class PhoneAuthenticationToken extends AbstractAuthenticationToken {

    private final String phone;
    private final String smsCode;
    private final UserDetails principal;

    // Конструктор до проверки
    public PhoneAuthenticationToken(String phone, String smsCode) {
        super(null);
        this.phone = phone;
        this.smsCode = smsCode;
        this.principal = null;
        setAuthenticated(false);
    }

    // Конструктор после проверки
    public PhoneAuthenticationToken(UserDetails principal) {
        super(principal.getAuthorities());
        this.phone = principal.getUsername();
        this.smsCode = null;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return smsCode; }

    @Override
    public Object getPrincipal() { return principal != null ? principal : phone; }
}
