package cl.duocuc.induztek.protocols.service;

import cl.duocuc.induztek.protocols.client.UsuarioClient;
import cl.duocuc.induztek.protocols.dto.ProtocoloRequest;
import cl.duocuc.induztek.protocols.dto.ProtocoloResponse;
import cl.duocuc.induztek.protocols.model.Protocolo;
import cl.duocuc.induztek.protocols.repository.ProtocoloRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

@Service
public class ProtocoloService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final ProtocoloRepository repository;
    private final UsuarioClient usuarioClient;

    public ProtocoloService(ProtocoloRepository repository, UsuarioClient usuarioClient) {
        this.repository = repository;
        this.usuarioClient = usuarioClient;
    }

    @Transactional(readOnly = true)
    public List<ProtocoloResponse> listar(String correoUsuario) {
        String correo = normalizarCorreo(correoUsuario);
        return repository.findAllByCorreoUsuarioIgnoreCaseOrderByFechaDescIdDesc(correo).stream()
                .map(ProtocoloResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProtocoloResponse obtener(Long id, String correoUsuario) {
        return ProtocoloResponse.from(buscarPropio(id, normalizarCorreo(correoUsuario)));
    }

    public ProtocoloResponse crear(ProtocoloRequest request) {
        String correo = normalizarCorreo(request.correoUsuario());
        LocalDate fecha = parsearFecha(request.fecha());
        usuarioClient.validarUsuarioAprobado(correo);
        Protocolo protocolo = new Protocolo(limpiarRequerido(request.equipo()), fecha, request.estado(),
                limpiarOpcional(request.observaciones()), correo, limpiarOpcional(request.temperatura()),
                limpiarOpcional(request.humedad()));
        return ProtocoloResponse.from(repository.save(protocolo));
    }

    @Transactional
    public ProtocoloResponse actualizar(Long id, String correoUsuario, ProtocoloRequest request) {
        String correo = normalizarCorreo(correoUsuario);
        Protocolo protocolo = buscarPropio(id, correo);
        if (!normalizarCorreo(request.correoUsuario()).equals(correo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede cambiar el propietario del protocolo");
        }
        protocolo.actualizar(limpiarRequerido(request.equipo()), parsearFecha(request.fecha()),
                request.estado(), limpiarOpcional(request.observaciones()),
                limpiarOpcional(request.temperatura()), limpiarOpcional(request.humedad()));
        return ProtocoloResponse.from(repository.save(protocolo));
    }

    @Transactional
    public void eliminar(Long id, String correoUsuario) {
        repository.delete(buscarPropio(id, normalizarCorreo(correoUsuario)));
    }

    private Protocolo buscarPropio(Long id, String correoUsuario) {
        return repository.findByIdAndCorreoUsuarioIgnoreCase(id, correoUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Protocolo no encontrado para este usuario"));
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo del usuario es obligatorio");
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha debe ser válida y tener formato dd/MM/yyyy");
        }
        try {
            return LocalDate.parse(fecha.trim(), DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha debe ser válida y tener formato dd/MM/yyyy");
        }
    }

    private String limpiarRequerido(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del equipo es obligatorio");
        }
        return valor.trim();
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
