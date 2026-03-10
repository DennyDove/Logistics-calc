package com.denidove.Logistics.exceptions;

public class DellineBadRequestException extends AppException {
    public DellineBadRequestException(String message) {
        super(400,"DELLINE_ERROR", message);
    }
}
