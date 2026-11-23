package cl.duocuc.induztek.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(min = 3, max = 120) String nombre,
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(min = 6, max = 72) String contrasena,
        @Size(max = 12) String rut,
        @Size(max = 30) String telefono,
        @Size(max = 100) String cargo
) {
}
