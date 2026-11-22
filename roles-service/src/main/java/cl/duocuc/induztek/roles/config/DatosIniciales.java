package cl.duocuc.induztek.roles.config;

import cl.duocuc.induztek.roles.model.Rol;
import cl.duocuc.induztek.roles.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner inicializarRoles(RolRepository repository) {
        return args -> {
            List.of(
                    new Rol("Administrador", "Administración de la plataforma"),
                    new Rol("Jefe técnico", "Supervisión de operaciones técnicas"),
                    new Rol("Ingeniero", "Gestión e inspección de equipos"),
                    new Rol("Técnico", "Registro de inspecciones en terreno"),
                    new Rol("Cliente", "Consulta de servicios contratados")
            ).forEach(rol -> {
                if (!repository.existsByNombreIgnoreCase(rol.getNombre())) {
                    repository.save(rol);
                }
            });
        };
    }
}
