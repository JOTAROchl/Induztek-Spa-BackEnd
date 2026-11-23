package cl.duocuc.induztek.users.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> manejarEstado(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return ResponseEntity.status(status).body(error(status, exception.getReason(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException exception,
                                                       HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::mensajeCampo).collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, message, request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(HttpMessageNotReadableException exception,
                                                          HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(error(HttpStatus.BAD_REQUEST, "El cuerpo JSON no tiene un formato válido", request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarDuplicado(DataIntegrityViolationException exception,
                                                      HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(HttpStatus.CONFLICT, "El registro entra en conflicto con un dato existente", request));
    }

    private String mensajeCampo(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ApiError error(HttpStatus status, String message, HttpServletRequest request) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
    }

    public record ApiError(Instant timestamp, int status, String error, String message, String path) {
    }
}
