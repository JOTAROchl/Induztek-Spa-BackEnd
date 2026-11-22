package cl.duocuc.induztek.roles.repository;

import cl.duocuc.induztek.roles.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findAllByActivoTrueOrderByNombreAsc();

    Optional<Rol> findByIdAndActivoTrue(Long id);

    boolean existsByNombreIgnoreCase(String nombre);
}
