package com.denidove.Logistics.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

public class VerificationCodeErrorException extends AuthenticationException {
                                                    // выдает параметр ?error через AuthenticationFailureHandler

    public VerificationCodeErrorException(String message) {
        super(message);
    }
}
