package cl.duocuc.induztek.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @NotBlank @Size(min = 3, max = 120) String nombre,
        @NotBlank @Email @Size(max = 254) String correo,
        @Size(max = 12) String rut,
        @Size(max = 30) String telefono,
        @Size(max = 100) String cargo
) {
}
