package com.denidove.Logistics.exceptions;

public class IncorrectDimensionException extends AppException {
    public IncorrectDimensionException(String message) {
        super(400, "INCORRECT_DIMENSION", message);
    }
}