package cl.duocuc.induztek.protocols.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDto(Long id, String correo, boolean aprobado) {
}
