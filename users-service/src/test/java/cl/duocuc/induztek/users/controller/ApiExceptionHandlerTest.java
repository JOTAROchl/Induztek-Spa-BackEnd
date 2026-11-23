package cl.duocuc.induztek.users.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/usuarios");
    }

    @Test
    void responseStatusPreservaCodigoYMensaje() {
        var response = handler.manejarEstado(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuario no encontrado", response.getBody().message());
        assertEquals("/api/usuarios", response.getBody().path());
    }

    @Test
    void erroresDeValidacionIncluyenCampo() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "correo", "debe tener formato de correo"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, binding);

        var response = handler.manejarValidacion(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("correo: debe tener formato de correo", response.getBody().message());
    }

    @Test
    void jsonInvalidoDevuelveBadRequest() {
        var response = handler.manejarJsonInvalido(mock(HttpMessageNotReadableException.class), request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("El cuerpo JSON no tiene un formato válido", response.getBody().message());
    }

    @Test
    void conflictoDeBaseDevuelveConflict() {
        var response = handler.manejarDuplicado(new DataIntegrityViolationException("duplicate"), request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("/api/usuarios", response.getBody().path());
    }
}
