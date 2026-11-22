package cl.duocuc.induztek.users.service;

import cl.duocuc.induztek.users.client.RolClient;
import cl.duocuc.induztek.users.dto.ActualizarUsuarioRequest;
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

import java.util.List;
import java.util.Locale;

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
                ? repository.findAllByActivoTrueOrderByNombreAsc()
                : repository.findAllByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCorreoContainingIgnoreCase(
                        buscar.trim(), buscar.trim());
        return usuarios.stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return respuesta(buscarActivo(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCorreo(String correo) {
        Usuario usuario = repository.findByCorreoIgnoreCaseAndActivoTrue(normalizarCorreo(correo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return respuesta(usuario);
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        String correo = normalizarCorreo(request.correo());
        if (repository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        rolClient.obtenerActivo(request.rolId());
        Usuario usuario = new Usuario(request.nombre().trim(), correo,
                passwordEncoder.encode(request.contrasena()), request.rolId());
        return respuesta(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = buscarPorCorreo(request.correo());
        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasenaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        return respuesta(usuario);
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarActivo(id);
        String correo = normalizarCorreo(request.correo());
        if (!usuario.getCorreo().equals(correo) && repository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        RolDto rol = rolClient.obtenerActivo(request.rolId());
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setRolId(rol.id());
        return respuesta(repository.save(usuario), rol.nombre());
    }

    public void desactivar(Long id) {
        Usuario usuario = buscarActivo(id);
        usuario.setActivo(false);
        repository.save(usuario);
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        return respuesta(usuario, rolClient.obtenerActivo(usuario.getRolId()).nombre());
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
