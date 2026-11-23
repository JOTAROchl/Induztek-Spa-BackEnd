package cl.duocuc.induztek.users.controller;

import cl.duocuc.induztek.users.dto.ActualizarUsuarioRequest;
import cl.duocuc.induztek.users.dto.AprobacionRequest;
import cl.duocuc.induztek.users.dto.CambiarContrasenaRequest;
import cl.duocuc.induztek.users.dto.CambiarRolRequest;
import cl.duocuc.induztek.users.dto.LoginRequest;
import cl.duocuc.induztek.users.dto.RegistroRequest;
import cl.duocuc.induztek.users.dto.UsuarioResponse;
import cl.duocuc.induztek.users.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) String buscar) {
        return service.listar(buscar);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/by-email/{correo}")
    public UsuarioResponse obtenerPorCorreo(@PathVariable String correo) {
        return service.obtenerPorCorreo(correo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return service.iniciarSesion(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request) {
        return service.actualizar(id, request);
    }

    @PutMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest request) {
        return service.cambiarRol(id, request.rolId());
    }

    @PatchMapping("/{id}/aprobacion")
    public UsuarioResponse cambiarAprobacion(@PathVariable Long id, @Valid @RequestBody AprobacionRequest request) {
        return service.cambiarAprobacion(id, request.aprobado());
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Long id, @Valid @RequestBody CambiarContrasenaRequest request) {
        service.cambiarContrasena(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }
}
