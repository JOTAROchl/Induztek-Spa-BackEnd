package cl.duocuc.induztek.users.dto;

import jakarta.validation.constraints.NotNull;

public record AprobacionRequest(@NotNull Boolean aprobado) {
}
