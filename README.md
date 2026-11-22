# Induztek SpA — Backend de microservicios

Backend independiente para la app Android Induztek SpA. Sigue el patrón del ejemplo
`MiRegistro_Microservicios`: servicios Spring Boot separados y comunicación entre ellos
por HTTP con `WebClient`.

| Servicio | Puerto HTTP | Base de datos exclusiva | Responsabilidad |
|---|---:|---|---|
| `roles-service` | 8081 | `induztek_roles` | Catálogo de roles |
| `users-service` | 8082 | `induztek_users` | Registro, consulta y login de usuarios |
| `protocols-service` | 8083 | `induztek_protocols` | Protocolos e inspecciones de terreno |

Cada servicio tiene su propio contenedor MySQL, credenciales, esquema y repositorios.
No hay claves foráneas entre servicios ni acceso a las bases de datos de otro servicio.
Las relaciones se representan con identificadores/correos y se validan por API:

- `users-service` consulta `roles-service` con `WebClient` al asignar o devolver un rol.
- `protocols-service` consulta `users-service` con `WebClient` para confirmar al responsable.

Los valores de URL y credenciales se configuran por variables de entorno. El archivo
`.env` no se versiona. El backend no crea usuarios ni contraseñas de demostración al iniciar.

## Requisitos

- JDK 21
- Maven 3.9+
- Docker Desktop con Docker Compose (para levantar las tres bases aisladas)

## Ejecución con Docker Compose

Desde esta carpeta:

```shell
Copy-Item .env.example .env
# Reemplazar los valores de .env por contraseñas locales únicas
docker compose up --build
```

Compose compila los tres módulos, espera a que cada base de datos esté saludable y luego
inicia los servicios. Para detenerlos:

```shell
docker compose down
```

Los datos persisten en tres volúmenes separados. Para borrarlos intencionalmente:
`docker compose down -v`.

## Ejecución local

1. Copia `.env.example` a `.env` y reemplaza los valores de ejemplo por contraseñas locales.
2. Inicia solo las bases: `docker compose up -d roles-db users-db protocols-db`.
3. En una terminal por servicio, configura la contraseña con el valor equivalente de
   `.env` y ejecuta el comando correspondiente.

```shell
$env:DB_PASSWORD="<valor de ROLES_DB_PASSWORD>"
mvn -pl roles-service spring-boot:run
```

```shell
$env:DB_PASSWORD="<valor de USERS_DB_PASSWORD>"
$env:ROLES_SERVICE_URL="http://localhost:8081"
mvn -pl users-service spring-boot:run
```

```shell
$env:DB_PASSWORD="<valor de PROTOCOLS_DB_PASSWORD>"
$env:USERS_SERVICE_URL="http://localhost:8082"
mvn -pl protocols-service spring-boot:run
```

La configuración local usa los puertos MySQL 3307, 3308 y 3309 respectivamente.
Las URLs entre servicios apuntan a `localhost`; se pueden sobrescribir con
`ROLES_SERVICE_URL` y `USERS_SERVICE_URL`.

## API

Todas las respuestas usan JSON. Los errores de validación y negocio devuelven un objeto
`{"status": ..., "error": ..., "message": ..., "path": ..., "timestamp": ...}`.

### roles-service — `http://localhost:8081`

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/roles` | Roles activos |
| GET | `/api/roles/{id}` | Rol o 404 |
| POST | `/api/roles` | Crear rol, 201; 409 si el nombre existe |
| PUT | `/api/roles/{id}` | Actualizar rol |
| DELETE | `/api/roles/{id}` | Desactivar rol (borrado lógico) |

### users-service — `http://localhost:8082`

| Método | Ruta | Resultado |
|---|---|---|
| POST | `/api/usuarios` | Registrar usuario, 201 |
| POST | `/api/usuarios/login` | Validar correo y contraseña |
| GET | `/api/usuarios?buscar=texto` | Buscar por nombre o correo |
| GET | `/api/usuarios/{id}` | Usuario o 404 |
| GET | `/api/usuarios/by-email/{correo}` | Buscar responsable para integración interna |
| PUT | `/api/usuarios/{id}` | Actualizar nombre, correo o rol |
| DELETE | `/api/usuarios/{id}` | Desactivar usuario |

Las contraseñas se guardan con BCrypt y nunca aparecen en las respuestas. Para registrar:

```json
{
  "nombre": "Ana Torres",
  "correo": "ana@induztek.cl",
  "contrasena": "UnaClaveSegura123!",
  "rolId": 1
}
```

### protocols-service — `http://localhost:8083`

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/protocolos?correoUsuario=ana@induztek.cl` | Protocolos del responsable |
| GET | `/api/protocolos/{id}?correoUsuario=...` | Protocolo del responsable o 404 |
| POST | `/api/protocolos` | Crear; verifica al responsable con `users-service` |
| PUT | `/api/protocolos/{id}?correoUsuario=...` | Actualizar protocolo propio |
| DELETE | `/api/protocolos/{id}?correoUsuario=...` | Eliminar protocolo propio |

Ejemplo de creación (fechas ISO-8601):

```json
{
  "equipo": "TR-01",
  "fecha": "2026-11-22",
  "estado": "Registrado en terreno",
  "observaciones": "Inspección visual completada",
  "correoUsuario": "ana@induztek.cl",
  "temperatura": 22.5,
  "humedad": 45
}
```

Los estados admitidos son `Borrador`, `Registrado en terreno`, `Observado`,
`Formalizado` y `Anulado`. Temperatura y humedad son opcionales; humedad debe estar
entre 0 y 100.

## Límites de seguridad

El login valida credenciales y almacena hashes BCrypt, pero este primer backend no emite
tokens ni implementa autorización por rol. La consulta de protocolos queda limitada al
correo indicado, pero esa identidad todavía no está autenticada por token. Antes de
exponerlo en Internet, integrar JWT/OAuth2 y aplicar autorización en todos los servicios.
