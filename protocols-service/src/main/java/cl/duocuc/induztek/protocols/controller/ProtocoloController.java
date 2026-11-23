package cl.duocuc.induztek.protocols.controller;

import cl.duocuc.induztek.protocols.dto.ProtocoloRequest;
import cl.duocuc.induztek.protocols.dto.ProtocoloResponse;
import cl.duocuc.induztek.protocols.service.ProtocoloService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/protocolos")
@Tag(name = "Protocolos", description = "Registro y consulta de inspecciones técnicas")
public class ProtocoloController {

    private final ProtocoloService service;

    public ProtocoloController(ProtocoloService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProtocoloResponse> listar(
            @RequestParam @NotBlank @Email String correoUsuario) {
        return service.listar(correoUsuario);
    }

    @GetMapping("/{id}")
    public ProtocoloResponse obtener(@PathVariable Long id,
                                     @RequestParam @NotBlank @Email String correoUsuario) {
        return service.obtener(id, correoUsuario);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProtocoloResponse crear(@NotNull @Valid @RequestBody ProtocoloRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ProtocoloResponse actualizar(@PathVariable Long id,
                                        @RequestParam @NotBlank @Email String correoUsuario,
                                        @NotNull @Valid @RequestBody ProtocoloRequest request) {
        return service.actualizar(id, correoUsuario, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id,
                         @RequestParam @NotBlank @Email String correoUsuario) {
        service.eliminar(id, correoUsuario);
    }
}
