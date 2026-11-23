package cl.duocuc.induztek.protocols.controller;

import cl.duocuc.induztek.protocols.dto.ProtocoloRequest;
import cl.duocuc.induztek.protocols.dto.ProtocoloResponse;
import cl.duocuc.induztek.protocols.service.ProtocoloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProtocoloController.class)
@Import(ApiExceptionHandler.class)
class ProtocoloApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProtocoloService service;

    @Test
    void creaProtocoloValidadoConEstadoCreated() throws Exception {
        when(service.crear(any(ProtocoloRequest.class))).thenReturn(
                new ProtocoloResponse(1L, "TR-01", "23/11/2026", "Borrador",
                        null, "ana@induztek.cl", null, null, null, null));

        mockMvc.perform(post("/api/protocolos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipo": "TR-01",
                                  "fecha": "23/11/2026",
                                  "estado": "Borrador",
                                  "correoUsuario": "ana@induztek.cl"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fecha").value("23/11/2026"));
    }

    @Test
    void rechazaEstadosNoPermitidos() throws Exception {
        mockMvc.perform(post("/api/protocolos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipo": "TR-01",
                                  "fecha": "23/11/2026",
                                  "estado": "Cerrado",
                                  "correoUsuario": "ana@induztek.cl"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("estado")));
    }

    @Test
    void rechazaCuerpoJsonNulo() throws Exception {
        mockMvc.perform(post("/api/protocolos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiereCorreoValidoParaListarProtocolos() throws Exception {
        mockMvc.perform(get("/api/protocolos").param("correoUsuario", "correo-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
