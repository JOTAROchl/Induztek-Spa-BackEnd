package cl.duocuc.induztek.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CambiarRolRequest(@NotNull @Positive Long rolId) {
}
