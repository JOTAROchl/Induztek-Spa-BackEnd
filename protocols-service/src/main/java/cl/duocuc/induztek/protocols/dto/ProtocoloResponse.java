package cl.duocuc.induztek.protocols.dto;

import cl.duocuc.induztek.protocols.model.Protocolo;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public record ProtocoloResponse(
        Long id,
        String equipo,
        String fecha,
        String estado,
        String observaciones,
        String correoUsuario,
        String temperatura,
        String humedad,
        Instant creadoEn,
        Instant actualizadoEn
) {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static ProtocoloResponse from(Protocolo protocolo) {
        return new ProtocoloResponse(protocolo.getId(), protocolo.getEquipo(),
                protocolo.getFecha().format(DATE_FORMAT), protocolo.getEstado(),
                protocolo.getObservaciones(), protocolo.getCorreoUsuario(),
                protocolo.getTemperatura(), protocolo.getHumedad(),
                protocolo.getCreadoEn(), protocolo.getActualizadoEn());
    }
}
