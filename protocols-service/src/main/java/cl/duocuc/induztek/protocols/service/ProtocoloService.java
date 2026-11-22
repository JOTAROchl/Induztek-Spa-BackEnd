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

import java.math.BigDecimal;
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
        BigDecimal temperatura = medicion(request.temperatura(), "temperatura",
                new BigDecimal("-50"), new BigDecimal("150"));
        BigDecimal humedad = medicion(request.humedad(), "humedad",
                BigDecimal.ZERO, new BigDecimal("100"));
        usuarioClient.obtenerActivo(correo);
        Protocolo protocolo = new Protocolo(request.equipo().trim(), request.fecha(),
                request.estado().trim(), limpiar(request.observaciones()), correo,
                temperatura, humedad);
        return ProtocoloResponse.from(repository.save(protocolo));
    }

    public ProtocoloResponse actualizar(Long id, String correoUsuario, ProtocoloRequest request) {
        validarEstado(request.estado());
        String correo = validarResponsable(correoUsuario);
        if (!correo.equals(validarResponsable(request.correoUsuario()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede transferir un protocolo a otro responsable");
        }
        BigDecimal temperatura = medicion(request.temperatura(), "temperatura",
                new BigDecimal("-50"), new BigDecimal("150"));
        BigDecimal humedad = medicion(request.humedad(), "humedad",
                BigDecimal.ZERO, new BigDecimal("100"));
        usuarioClient.obtenerActivo(correo);
        Protocolo protocolo = buscarPropio(id, correo);
        protocolo.setEquipo(request.equipo().trim());
        protocolo.setFecha(request.fecha());
        protocolo.setEstado(request.estado().trim());
        protocolo.setObservaciones(limpiar(request.observaciones()));
        protocolo.setTemperatura(temperatura);
        protocolo.setHumedad(humedad);
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

    private BigDecimal medicion(String valor, String nombre, BigDecimal minimo, BigDecimal maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        BigDecimal medicion;
        try {
            medicion = new BigDecimal(valor.trim());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nombre + " debe ser numérica");
        }
        if (medicion.compareTo(minimo) < 0 || medicion.compareTo(maximo) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    nombre + " está fuera del rango permitido");
        }
        return medicion;
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
