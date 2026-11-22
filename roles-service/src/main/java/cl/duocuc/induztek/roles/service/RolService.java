package cl.duocuc.induztek.roles.service;

import cl.duocuc.induztek.roles.dto.RolRequest;
import cl.duocuc.induztek.roles.dto.RolResponse;
import cl.duocuc.induztek.roles.model.Rol;
import cl.duocuc.induztek.roles.repository.RolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class RolService {

    private final RolRepository repository;

    public RolService(RolRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return repository.findAllByActivoTrueOrderByNombreAsc().stream()
                .map(RolResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RolResponse obtener(Long id) {
        return RolResponse.from(buscarActivo(id));
    }

    public RolResponse crear(RolRequest request) {
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un rol con ese nombre");
        }
        return RolResponse.from(repository.save(new Rol(nombre, limpiar(request.descripcion()))));
    }

    public RolResponse actualizar(Long id, RolRequest request) {
        Rol rol = buscarActivo(id);
        String nombre = request.nombre().trim();
        if (!rol.getNombre().equalsIgnoreCase(nombre) && repository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un rol con ese nombre");
        }
        rol.setNombre(nombre);
        rol.setDescripcion(limpiar(request.descripcion()));
        return RolResponse.from(repository.save(rol));
    }

    public void desactivar(Long id) {
        Rol rol = buscarActivo(id);
        rol.setActivo(false);
        repository.save(rol);
    }

    private Rol buscarActivo(Long id) {
        return repository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
