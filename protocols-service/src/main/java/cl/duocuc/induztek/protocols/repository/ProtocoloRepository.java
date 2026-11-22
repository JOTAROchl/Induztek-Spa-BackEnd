package cl.duocuc.induztek.protocols.repository;

import cl.duocuc.induztek.protocols.model.Protocolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProtocoloRepository extends JpaRepository<Protocolo, Long> {
    List<Protocolo> findAllByCorreoUsuarioIgnoreCaseOrderByFechaDescIdDesc(String correoUsuario);

    Optional<Protocolo> findByIdAndCorreoUsuarioIgnoreCase(Long id, String correoUsuario);
}
