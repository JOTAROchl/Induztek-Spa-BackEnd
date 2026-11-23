package cl.duocuc.induztek.protocols.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProtocoloRequest(
        @NotBlank @Size(max = 120) String equipo,
        @NotBlank @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}",
                message = "debe tener formato dd/MM/yyyy") String fecha,
        @NotBlank @Pattern(regexp = "Borrador|Registrado en terreno|Observado|Formalizado|Anulado",
                message = "no es un estado permitido") String estado,
        @Size(max = 5000) String observaciones,
        @NotBlank @Email @Size(max = 254) String correoUsuario,
        @Size(max = 20) String temperatura,
        @Size(max = 20) String humedad
) {
}
