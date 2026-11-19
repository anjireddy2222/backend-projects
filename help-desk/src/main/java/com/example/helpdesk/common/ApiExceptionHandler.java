package com.example.helpdesk.common;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(org.springframework.mail.MailException.class)
  ResponseEntity<?> handleMail(Exception ex) {
    return ResponseEntity.status(503)
        .body(Map.of("message", "Email service unavailable; try again later"));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> handleApi(ApiException ex) {
    return ResponseEntity.status(ex.status).body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    var errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
    return ResponseEntity.badRequest()
        .body(Map.of("message", "Validation failed", "errors", errors));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<?> handleInvalidInput(Exception ex) {
    return ResponseEntity.badRequest()
        .body(Map.of("message", "Invalid request value or JSON body"));
  }

  @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
  ResponseEntity<?> handleConcurrentEdit(Exception ex) {
    return ResponseEntity.status(409)
        .body(Map.of("message", "Record changed; reload and try again"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> handleConflict(DataIntegrityViolationException ex) {
    return ResponseEntity.status(409)
        .body(Map.of("message", "A record with these details already exists or is still in use"));
  }
}
