package com.denidove.Logistics.exceptions;

import java.time.LocalDateTime;

public record ApiError(
        //LocalDateTime timestamp,
        //int status,
        //String error,
        //String errorCode,
        String message,
        String path
) { }
