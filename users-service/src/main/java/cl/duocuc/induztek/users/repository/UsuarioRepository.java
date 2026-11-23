package cl.duocuc.induztek.users.repository;

import cl.duocuc.induztek.users.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAllByOrderByAprobadoAscNombreAsc();

    List<Usuario> findAllByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCorreoContainingIgnoreCaseOrderByNombreAsc(
            String nombre, String correo
    );

    Optional<Usuario> findByCorreoIgnoreCaseAndActivoTrue(String correo);

    Optional<Usuario> findByIdAndActivoTrue(Long id);

    boolean existsByCorreoIgnoreCase(String correo);
}
