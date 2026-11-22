package cl.duocuc.induztek.protocols.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProtocoloRequest(
        @NotBlank @Size(max = 120) String equipo,
        @NotNull @JsonFormat(pattern = "dd/MM/yyyy") LocalDate fecha,
        @NotBlank @Size(max = 40) String estado,
        @Size(max = 4000) String observaciones,
        @NotBlank @Email @Size(max = 254) String correoUsuario,
        @Size(max = 16) String temperatura,
        @Size(max = 16) String humedad
) {
}
