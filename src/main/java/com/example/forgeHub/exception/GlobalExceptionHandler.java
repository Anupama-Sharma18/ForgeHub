package com.example.forgeHub.exception;

import com.example.forgeHub.model.User;
import com.example.forgeHub.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<User>> handeUserException(UserNotFoundException exception)
    {

        ApiResponse response=ApiResponse.<User>builder()
                .success(false)
                .message(exception.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handMethodException(MethodArgumentNotValidException exception)
    {

        Map<String,String> error=new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(
                errors -> error.put(errors.getField(),errors.getDefaultMessage())
        );

        return ResponseEntity.ok(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateRecord(
            DuplicateResourceException exception) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
