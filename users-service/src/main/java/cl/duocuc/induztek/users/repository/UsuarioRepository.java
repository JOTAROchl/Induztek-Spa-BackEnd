package cl.duocuc.induztek.users.repository;

import cl.duocuc.induztek.users.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCaseAndActivoTrue(String correo);

    Optional<Usuario> findByIdAndActivoTrue(Long id);

    List<Usuario> findAllByActivoTrueOrderByNombreAsc();

    List<Usuario> findAllByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCorreoContainingIgnoreCase(
            String nombre, String correo
    );

    boolean existsByCorreoIgnoreCase(String correo);
}
