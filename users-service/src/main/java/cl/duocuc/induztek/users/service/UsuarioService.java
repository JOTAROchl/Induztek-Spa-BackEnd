package cl.duocuc.induztek.users.service;

import cl.duocuc.induztek.users.client.RolClient;
import cl.duocuc.induztek.users.dto.ActualizarUsuarioRequest;
import cl.duocuc.induztek.users.dto.CambiarContrasenaRequest;
import cl.duocuc.induztek.users.dto.LoginRequest;
import cl.duocuc.induztek.users.dto.RegistroRequest;
import cl.duocuc.induztek.users.dto.RolDto;
import cl.duocuc.induztek.users.dto.UsuarioResponse;
import cl.duocuc.induztek.users.model.Usuario;
import cl.duocuc.induztek.users.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;
    private final RolClient rolClient;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, RolClient rolClient, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.rolClient = rolClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(String buscar) {
        List<Usuario> usuarios = buscar == null || buscar.isBlank()
                ? repository.findAllByOrderByAprobadoAscNombreAsc()
                : repository.findAllByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCorreoContainingIgnoreCaseOrderByNombreAsc(
                        buscar.trim(), buscar.trim());
        Map<Long, String> roles = rolClient.listarActivos().stream()
                .collect(Collectors.toMap(RolDto::id, RolDto::nombre));
        return usuarios.stream()
                .map(usuario -> respuesta(usuario, roles.getOrDefault(usuario.getRolId(), "Rol no disponible")))
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario usuario = buscarActivo(id);
        return respuesta(usuario, rolClient.obtenerActivo(usuario.getRolId()).nombre());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCorreo(String correo) {
        Usuario usuario = repository.findByCorreoIgnoreCaseAndActivoTrue(normalizarCorreo(correo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return respuesta(usuario, rolClient.obtenerActivo(usuario.getRolId()).nombre());
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        String correo = normalizarCorreo(request.correo());
        if (repository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        validarLimiteContrasena(request.contrasena());
        RolDto rol = rolClient.rolTecnicoPredeterminado();
        Usuario nuevo = new Usuario(request.nombre().trim(), correo,
                passwordEncoder.encode(request.contrasena()), limpiar(request.rut()),
                limpiar(request.telefono()), valorPredeterminado(request.cargo(), "Técnico de Terreno"), rol.id());
        Usuario guardado = repository.save(nuevo);
        return respuesta(guardado, rol.nombre());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = buscarPorCorreo(request.correo());
        if (!cabeEnBCrypt(request.contrasena())
                || !passwordEncoder.matches(request.contrasena(), usuario.getContrasenaHash())) {
            throw credencialesIncorrectas();
        }
        return respuesta(usuario, rolClient.obtenerActivo(usuario.getRolId()).nombre());
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarActivo(id);
        String correo = normalizarCorreo(request.correo());
        if (!usuario.getCorreo().equals(correo) && repository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setRut(limpiar(request.rut()));
        usuario.setTelefono(limpiar(request.telefono()));
        usuario.setCargo(valorPredeterminado(request.cargo(), "Técnico de Terreno"));
        return respuesta(repository.save(usuario), rolClient.obtenerActivo(usuario.getRolId()).nombre());
    }

    public UsuarioResponse cambiarRol(Long id, Long rolId) {
        Usuario usuario = buscarActivo(id);
        RolDto rol = rolClient.obtenerActivo(rolId);
        usuario.setRolId(rol.id());
        return respuesta(repository.save(usuario), rol.nombre());
    }

    public UsuarioResponse cambiarAprobacion(Long id, boolean aprobado) {
        Usuario usuario = buscarActivo(id);
        usuario.setAprobado(aprobado);
        return respuesta(repository.save(usuario), rolClient.obtenerActivo(usuario.getRolId()).nombre());
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest request) {
        Usuario usuario = buscarActivo(id);
        if (!passwordEncoder.matches(request.actual(), usuario.getContrasenaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta");
        }
        validarLimiteContrasena(request.nueva());
        usuario.setContrasenaHash(passwordEncoder.encode(request.nueva()));
        repository.save(usuario);
    }

    public void desactivar(Long id) {
        Usuario usuario = buscarActivo(id);
        usuario.setActivo(false);
        repository.save(usuario);
    }

    private UsuarioResponse respuesta(Usuario usuario, String rolNombre) {
        return UsuarioResponse.from(usuario, rolNombre);
    }

    private Usuario buscarActivo(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Usuario buscarPorCorreo(String correo) {
        return repository.findByCorreoIgnoreCaseAndActivoTrue(normalizarCorreo(correo))
                .orElseThrow(this::credencialesIncorrectas);
    }

    private ResponseStatusException credencialesIncorrectas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private String valorPredeterminado(String texto, String valor) {
        String limpio = limpiar(texto);
        return limpio == null ? valor : limpio;
    }

    private void validarLimiteContrasena(String contrasena) {
        if (!cabeEnBCrypt(contrasena)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña no puede superar 72 bytes en UTF-8");
        }
    }

    private boolean cabeEnBCrypt(String contrasena) {
        return contrasena.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
