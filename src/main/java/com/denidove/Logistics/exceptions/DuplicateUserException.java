package com.denidove.Logistics.exceptions;

public class DuplicateUserException extends RuntimeException {

    private final String code;

    public DuplicateUserException(String code) {
        super(code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
