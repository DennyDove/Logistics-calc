package com.denidove.Logistics.exceptions;

public class DellineRequestException extends RuntimeException {
    private static String price;

    public DellineRequestException(String message) {
        super(message);
    }
}
