package cl.duocuc.induztek.protocols.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import cl.duocuc.induztek.protocols.model.Protocolo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ProtocoloResponse(
        Long id,
        String equipo,
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate fecha,
        String estado,
        String observaciones,
        String correoUsuario,
        BigDecimal temperatura,
        BigDecimal humedad,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static ProtocoloResponse from(Protocolo protocolo) {
        return new ProtocoloResponse(protocolo.getId(), protocolo.getEquipo(), protocolo.getFecha(),
                protocolo.getEstado(), protocolo.getObservaciones(), protocolo.getCorreoUsuario(),
                protocolo.getTemperatura(), protocolo.getHumedad(),
                protocolo.getCreadoEn(), protocolo.getActualizadoEn());
    }
}
