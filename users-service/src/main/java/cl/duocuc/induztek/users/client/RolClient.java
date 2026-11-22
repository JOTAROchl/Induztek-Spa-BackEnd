package cl.duocuc.induztek.users.client;

import cl.duocuc.induztek.users.dto.RolDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RolClient {

    private final WebClient client;

    public RolClient(WebClient.Builder builder, @Value("${roles.service.url}") String rolesUrl) {
        this.client = builder.baseUrl(rolesUrl).build();
    }

    public RolDto obtenerActivo(Long rolId) {
        try {
            RolDto rol = client.get()
                    .uri("/api/roles/{id}", rolId)
                    .retrieve()
                    .bodyToMono(RolDto.class)
                    .block();
            if (rol == null || !rol.activo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol no existe o está inactivo");
            }
            return rol;
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol no existe o está inactivo");
        } catch (WebClientRequestException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de roles no está disponible");
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible consultar el servicio de roles");
        }
    }
}
