package cl.duocuc.induztek.protocols.client;

import cl.duocuc.induztek.protocols.dto.UsuarioDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UsuarioClient {

    private final WebClient client;

    public UsuarioClient(WebClient.Builder builder, @Value("${users.service.url}") String usersUrl) {
        this.client = builder.baseUrl(usersUrl).build();
    }

    public UsuarioDto obtenerActivo(String correo) {
        try {
            UsuarioDto usuario = client.get()
                    .uri("/api/usuarios/by-email/{correo}", correo)
                    .retrieve()
                    .bodyToMono(UsuarioDto.class)
                    .block();
            if (usuario == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario responsable no está disponible");
            }
            return usuario;
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario responsable no existe o está inactivo");
        } catch (WebClientRequestException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de usuarios no está disponible");
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible consultar el servicio de usuarios");
        }
    }
}
