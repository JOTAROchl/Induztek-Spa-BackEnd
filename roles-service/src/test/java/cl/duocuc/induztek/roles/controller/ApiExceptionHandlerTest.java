package cl.duocuc.induztek.roles.controller;

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
        when(request.getRequestURI()).thenReturn("/api/roles");
    }

    @Test
    void conservaEstadoMensajeYRuta() {
        var response = handler.manejarEstado(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Rol no encontrado", response.getBody().message());
        assertEquals("/api/roles", response.getBody().path());
    }

    @Test
    void devuelveDetalleDeCampoInvalido() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, binding);

        var response = handler.manejarValidacion(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("nombre: no debe estar vacío", response.getBody().message());
    }

    @Test
    void jsonInvalidoDevuelveBadRequest() {
        var response = handler.manejarJsonInvalido(mock(HttpMessageNotReadableException.class), request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void conflictoDePersistenciaDevuelveConflict() {
        var response = handler.manejarDuplicado(new DataIntegrityViolationException("duplicate"), request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("El registro entra en conflicto con un dato existente", response.getBody().message());
    }
}
