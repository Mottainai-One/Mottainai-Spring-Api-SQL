package com.institutojf.mottainai.handler;

import com.institutojf.mottainai.exception.BusinessException;
import com.institutojf.mottainai.exception.ConflictException;
import com.institutojf.mottainai.exception.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Database connection exception
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiError> handleSqlException(SQLException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                503,
                "SERVICE_UNAVAILABLE",
                "Database service is temporarily unavailable",
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // Database constraint violation
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                409,
                "CONFLICT",
                "The operation violates an existing data constraint",
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Generic database access exception
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(DataAccessException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                500,
                "INTERNAL_SERVER_ERROR",
                "Unable to process the request due to a database error",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Resource not found exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                404,
                "NOT_FOUND",
                exception.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Conflict exception raised by a business rule
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                409,
                "CONFLICT",
                exception.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Business rule exception
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                400,
                "BAD_REQUEST",
                exception.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Request body validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                400,
                "BAD_REQUEST",
                "One or more fields are invalid",
                exception.getBindingResult().getFieldErrors()
                        .stream()
                        .map(fieldError -> new FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                        .toList()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Invalid path variable or query parameter type
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                400,
                "BAD_REQUEST",
                "Invalid value for parameter: " + exception.getName(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                400,
                "BAD_REQUEST",
                "Malformed request body",
                null
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFoundException(NoResourceFoundException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                404,
                "NOT_FOUND",
                "Resource not found",
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                401,
                "UNAUTHORIZED",
                "Invalid email or password",
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Generic runtime exception
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                500,
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception exception) {
        var error = new ApiError(
                LocalDateTime.now(),
                500,
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
