package cl.duocuc.induztek.users.dto;

import cl.duocuc.induztek.users.model.Usuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String rut,
        String telefono,
        String cargo,
        Long rolId,
        String rolNombre,
        boolean aprobado,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static UsuarioResponse from(Usuario usuario, String rolNombre) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                usuario.getRut(), usuario.getTelefono(), usuario.getCargo(), usuario.getRolId(),
                rolNombre, usuario.isAprobado(), usuario.getCreadoEn(), usuario.getActualizadoEn());
    }
}
