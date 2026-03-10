package com.denidove.Logistics.exceptions_old1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CalcRequestException extends RuntimeException {
    public CalcRequestException(String msg) { super(msg); }
}
