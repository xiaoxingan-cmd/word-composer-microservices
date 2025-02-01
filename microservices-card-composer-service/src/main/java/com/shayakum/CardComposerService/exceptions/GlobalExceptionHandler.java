package com.shayakum.CardComposerService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderAssemblerTimeoutException.class)
    public ResponseEntity<Object> handleOrderAssemblerTimeoutException(OrderAssemblerTimeoutException e) {
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body("Timeout exceeded while waiting for orderAssembler to be ready.");
    }
}
