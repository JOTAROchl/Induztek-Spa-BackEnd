package cl.duocuc.induztek.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(min = 10, max = 72) String contrasena,
        @NotNull @Positive Long rolId
) {
}
