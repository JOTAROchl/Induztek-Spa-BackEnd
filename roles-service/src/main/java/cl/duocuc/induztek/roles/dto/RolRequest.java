package cl.duocuc.induztek.roles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 250) String descripcion
) {
}
