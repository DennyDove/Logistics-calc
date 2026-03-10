package com.denidove.Logistics.exceptions_old1;

import org.springframework.http.HttpStatus;

public class DellineApiException extends RuntimeException {
    private final HttpStatus status;
    private final String body;

    public DellineApiException(HttpStatus status, String message, String body) {
        super(message);
        this.status = status;
        this.body = body;
    }

    public HttpStatus getStatus() { return status; }
    public String getBody() { return body; }
}
