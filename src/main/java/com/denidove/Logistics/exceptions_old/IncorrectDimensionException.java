package com.denidove.Logistics.exceptions_old;

public class IncorrectDimensionException extends RuntimeException {
    private static String price;

    public IncorrectDimensionException(String message) {
        super(message);
    }
}
