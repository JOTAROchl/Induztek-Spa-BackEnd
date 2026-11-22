package cl.duocuc.induztek.roles.dto;

import cl.duocuc.induztek.roles.model.Rol;

import java.time.Instant;

public record RolResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static RolResponse from(Rol rol) {
        return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion(),
                rol.isActivo(), rol.getCreadoEn(), rol.getActualizadoEn());
    }
}
