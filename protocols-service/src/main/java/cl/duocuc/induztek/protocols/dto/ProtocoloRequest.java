package cl.duocuc.induztek.protocols.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProtocoloRequest(
        @NotBlank @Size(max = 120) String equipo,
        @NotNull LocalDate fecha,
        @NotBlank @Size(max = 40) String estado,
        @Size(max = 4000) String observaciones,
        @NotBlank @Email @Size(max = 254) String correoUsuario,
        @DecimalMin("-50.00") @DecimalMax("150.00") BigDecimal temperatura,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal humedad
) {
}
