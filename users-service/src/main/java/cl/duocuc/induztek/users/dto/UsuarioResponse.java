package cl.duocuc.induztek.users.dto;

import cl.duocuc.induztek.users.model.Usuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        Long rolId,
        String rolNombre,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static UsuarioResponse from(Usuario usuario, String rolNombre) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                usuario.getRolId(), rolNombre, usuario.getCreadoEn(), usuario.getActualizadoEn());
    }
}
