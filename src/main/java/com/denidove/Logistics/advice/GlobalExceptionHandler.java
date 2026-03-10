package com.denidove.Logistics.advice;


import com.denidove.Logistics.dto.ErrorResponse;
import com.denidove.Logistics.exceptions.ApiError;
import com.denidove.Logistics.exceptions.AppException;
import com.denidove.Logistics.exceptions.DuplicateUserException;
import com.denidove.Logistics.postreq.impl.DellineServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(AppException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
                //LocalDateTime.now(),
                //ex.getStatus(),
                //getErrorName(ex.getStatus()),
                //ex.getErrorCode(),
                ex.getMessage(),
                req.getRequestURI()
        );

        // Логируем только WARN, потому что это ожидаемые ошибки
        log.warn("Handled AppException: status={}, code={}, uri={}, message={}",
                ex.getStatus(), ex.getErrorCode(), req.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception ex, HttpServletRequest req) {
        log.error("Unexpected server error", ex);

        ApiError error = new ApiError(
                "Внутренняя ошибка сервера",
                req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<?> handleDuplicateUserException(DuplicateUserException ex) {
        log.error("Unexpected server error", ex);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getCode()));
    }

    //toDo этот блок в новой архитектуре не нужен!
    /*

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest req) {

        ApiError error = new ApiError(
                //LocalDateTime.now(),
                //500,
                //"Internal Server Error",
                //"UNEXPECTED_ERROR",
                ex.getMessage(),
                req.getRequestURI()
        );

        return ResponseEntity.status(500).body(error);
    }

    private String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            default -> "Error";
        };
    }*/

}