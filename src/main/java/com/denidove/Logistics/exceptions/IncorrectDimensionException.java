package com.denidove.Logistics.exceptions;

public class IncorrectDimensionException extends RuntimeException {
    private static String price;

    public IncorrectDimensionException(String message) {
        super(message);
    }
}
