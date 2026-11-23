package cl.duocuc.induztek.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
        @NotBlank @Size(max = 72) String actual,
        @NotBlank @Size(min = 6, max = 72) String nueva
) {
}
