package com.rikkeibank.transaction.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> business(BusinessException ex, WebRequest req) {
        return build(ex.getStatus(), ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> denied(AccessDeniedException ex, WebRequest req) {
        return build(HttpStatus.FORBIDDEN, "Không đủ quyền truy cập", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex, WebRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> other(Exception ex, WebRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus s, String m, WebRequest req) {
        return ResponseEntity.status(s).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", s.value(),
                "error", s.getReasonPhrase(),
                "message", m == null ? "" : m,
                "path", req.getDescription(false).replace("uri=", "")));
    }
}
