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

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class ProtocoloService {

    private static final Set<String> ESTADOS = Set.of(
            "Borrador", "Registrado en terreno", "Observado", "Formalizado", "Anulado"
    );

    private final ProtocoloRepository repository;
    private final UsuarioClient usuarioClient;

    public ProtocoloService(ProtocoloRepository repository, UsuarioClient usuarioClient) {
        this.repository = repository;
        this.usuarioClient = usuarioClient;
    }

    @Transactional(readOnly = true)
    public List<ProtocoloResponse> listar(String correoUsuario) {
        String correo = validarResponsable(correoUsuario);
        usuarioClient.obtenerActivo(correo);
        return repository.findAllByCorreoUsuarioIgnoreCaseOrderByFechaDescIdDesc(correo)
                .stream().map(ProtocoloResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProtocoloResponse obtener(Long id, String correoUsuario) {
        return ProtocoloResponse.from(buscarPropio(id, correoUsuario));
    }

    public ProtocoloResponse crear(ProtocoloRequest request) {
        validarEstado(request.estado());
        String correo = validarResponsable(request.correoUsuario());
        usuarioClient.obtenerActivo(correo);
        Protocolo protocolo = new Protocolo(request.equipo().trim(), request.fecha(),
                request.estado().trim(), limpiar(request.observaciones()), correo,
                request.temperatura(), request.humedad());
        return ProtocoloResponse.from(repository.save(protocolo));
    }

    public ProtocoloResponse actualizar(Long id, String correoUsuario, ProtocoloRequest request) {
        validarEstado(request.estado());
        String correo = validarResponsable(correoUsuario);
        if (!correo.equals(validarResponsable(request.correoUsuario()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede transferir un protocolo a otro responsable");
        }
        usuarioClient.obtenerActivo(correo);
        Protocolo protocolo = buscarPropio(id, correo);
        protocolo.setEquipo(request.equipo().trim());
        protocolo.setFecha(request.fecha());
        protocolo.setEstado(request.estado().trim());
        protocolo.setObservaciones(limpiar(request.observaciones()));
        protocolo.setTemperatura(request.temperatura());
        protocolo.setHumedad(request.humedad());
        return ProtocoloResponse.from(repository.save(protocolo));
    }

    public void eliminar(Long id, String correoUsuario) {
        repository.delete(buscarPropio(id, correoUsuario));
    }

    private Protocolo buscarPropio(Long id, String correoUsuario) {
        String correo = validarResponsable(correoUsuario);
        return repository.findByIdAndCorreoUsuarioIgnoreCase(id, correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado"));
    }

    private String validarResponsable(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private void validarEstado(String estado) {
        if (!ESTADOS.contains(estado.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado del protocolo no es válido");
        }
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
