package com.anurag.lms.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

      private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
            Map<String, String> body = new HashMap<>();
            body.put("error", message);
            return ResponseEntity.status(status).body(body);
      }

      // 400 - @Valid failed: { "email": "Enter a valid email", ... }
      @ExceptionHandler(MethodArgumentNotValidException.class)
      public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getFieldErrors()
                        .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
      }

      @ExceptionHandler(BadRequestException.class)
      public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
            return error(HttpStatus.BAD_REQUEST, ex.getMessage());
      }

      @ExceptionHandler(ResourceNotFoundException.class)
      public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
      }

      @ExceptionHandler(ConflictException.class)
      public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
            return error(HttpStatus.CONFLICT, ex.getMessage());
      }

      @ExceptionHandler(BadCredentialsException.class)
      public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
      }

      // e.g. deleting a member who still has borrowing history
      @ExceptionHandler(DataIntegrityViolationException.class)
      public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException ex) {
            return error(HttpStatus.CONFLICT,
                        "This record is linked to other data (or duplicates a unique value), so the change was not applied");
      }

      @ExceptionHandler(OptimisticLockingFailureException.class)
      public ResponseEntity<Map<String, String>> handleLock(OptimisticLockingFailureException ex) {
            return error(HttpStatus.CONFLICT, "Someone else changed this record at the same time. Please try again");
      }
}
