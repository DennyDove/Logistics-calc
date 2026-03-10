package com.denidove.Logistics.exceptions_old;

import org.springframework.security.core.AuthenticationException;

public class VerificationCodeErrorException extends AuthenticationException {
                                                    // выдает параметр ?error через AuthenticationFailureHandler

    public VerificationCodeErrorException(String message) {
        super(message);
    }
}
