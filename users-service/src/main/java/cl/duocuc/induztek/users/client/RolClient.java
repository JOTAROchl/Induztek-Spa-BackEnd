package cl.duocuc.induztek.users.client;

import cl.duocuc.induztek.users.dto.RolDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class RolClient {

    private final WebClient client;

    public RolClient(WebClient.Builder builder, @Value("${roles.service.url}") String rolesUrl) {
        this.client = builder.baseUrl(rolesUrl).build();
    }

    public List<RolDto> listarActivos() {
        try {
            List<RolDto> roles = client.get()
                    .uri("/api/roles")
                    .retrieve()
                    .bodyToFlux(RolDto.class)
                    .collectList()
                    .block();
            if (roles == null) {
                throw servicioNoDisponible();
            }
            return roles.stream().filter(RolDto::activo).toList();
        } catch (WebClientRequestException exception) {
            throw servicioNoDisponible();
        } catch (WebClientResponseException exception) {
            throw servicioNoDisponible();
        }
    }

    public RolDto obtenerActivo(Long id) {
        try {
            RolDto rol = client.get()
                    .uri("/api/roles/{id}", id)
                    .retrieve()
                    .bodyToMono(RolDto.class)
                    .block();
            if (rol == null || !rol.activo()) {
                throw rolInvalido();
            }
            return rol;
        } catch (WebClientResponseException.NotFound exception) {
            throw rolInvalido();
        } catch (WebClientRequestException exception) {
            throw servicioNoDisponible();
        } catch (WebClientResponseException exception) {
            throw servicioNoDisponible();
        }
    }

    public RolDto rolTecnicoPredeterminado() {
        return listarActivos().stream()
                .filter(rol -> "Técnico".equalsIgnoreCase(rol.nombre().trim()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "El catálogo de roles no contiene el rol Técnico"));
    }

    private ResponseStatusException rolInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol no existe o está inactivo");
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de roles no está disponible");
    }
}
