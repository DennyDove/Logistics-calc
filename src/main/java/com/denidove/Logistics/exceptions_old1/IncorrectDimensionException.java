package com.denidove.Logistics.exceptions_old1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectDimensionException extends RuntimeException {
    public IncorrectDimensionException(String msg) { super(msg); }
}
