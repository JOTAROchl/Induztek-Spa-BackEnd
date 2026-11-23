package cl.duocuc.induztek.protocols.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final HttpServletRequest request = new MockHttpServletRequest("POST", "/api/protocolos");

    @Test
    void transformaEstadosHttpEnFormatoDeError() {
        var response = handler.manejarEstado(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No encontrado"), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("/api/protocolos", response.getBody().path());
    }

    @Test
    void muestraErroresDeValidacionDeCampos() {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "equipo", "", false, null, null, "no puede estar vacío"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(binding);

        var response = handler.manejarValidacion(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("equipo: no puede estar vacío", response.getBody().message());
    }

    @Test
    void informaParametroInvalido() {
        var response = handler.manejarParametroInvalido(new ConstraintViolationException(Set.of()), request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void informaJsonMalformado() {
        var response = handler.manejarJsonInvalido(mock(HttpMessageNotReadableException.class), request);

        assertEquals("El cuerpo JSON no tiene un formato válido", response.getBody().message());
    }

    @Test
    void informaConflictosDePersistencia() {
        var response = handler.manejarDuplicado(
                new DataIntegrityViolationException("duplicate"), request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("El registro entra en conflicto con un dato existente", response.getBody().message());
    }
}
