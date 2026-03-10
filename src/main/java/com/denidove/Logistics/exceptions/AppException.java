package com.denidove.Logistics.exceptions;

public class AppException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode; // <-- добавляем уникальный бизнес-код

    public AppException(int httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public int getStatus() { return httpStatus; }
    public String getErrorCode() { return errorCode; }
}
