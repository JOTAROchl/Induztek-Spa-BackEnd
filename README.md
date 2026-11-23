# Induztek SpA — microservicios (parche 4)

Este parche agrega `protocols-service` a `roles-service` y `users-service`. Sigue la
estructura Spring Boot + JPA + MySQL del ejemplo `MiRegistro_Microservicios`, manteniendo
base de datos, credenciales y propiedad de datos independientes en cada servicio.

## Arquitectura actual

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---:|---|---|
| `roles-service` | 8081 | `induztek_roles` | Catálogo de roles |
| `users-service` | 8082 | `induztek_users` | Registro, acceso y gestión de usuarios |
| `protocols-service` | 8083 | `induztek_protocolos` | Protocolos e inspecciones técnicas |

Cada servicio accede exclusivamente a su propia base de datos. `users-service` guarda
solo `rol_id` y consulta el nombre/validez del rol mediante HTTP con `WebClient`; nunca lee
las tablas de `roles-service`. `protocols-service` almacena el correo del responsable como
dato propio y consulta a `users-service` mediante `WebClient` al crear un protocolo, para
confirmar que la cuenta existe y está aprobada; no lee tablas ajenas. API Gateway y Eureka
aún no forman parte de esta etapa.

## Requisitos

- JDK 21
- Maven 3.9+
- MySQL local (Laragon o XAMPP); Docker Compose es opcional

## Ejecutar con Laragon o XAMPP

1. Inicia MySQL desde Laragon o XAMPP en `localhost:3306`, con el usuario `root` y
   contraseña vacía (valores habituales por defecto). Cada servicio crea y usa una base
   diferente.
2. Desde esta carpeta, inicia cada servicio en una terminal separada:

```shell
mvn -pl roles-service spring-boot:run
```

```powershell
$env:DB_NAME="induztek_users"
$env:DB_PORT="3306"
$env:DB_USER="root"
$env:DB_PASSWORD=""
$env:ROLES_SERVICE_URL="http://localhost:8081"
mvn -pl users-service spring-boot:run
```

```powershell
$env:DB_NAME="induztek_protocolos"
$env:DB_PORT="3306"
$env:DB_USER="root"
$env:DB_PASSWORD=""
$env:USERS_SERVICE_URL="http://localhost:8082"
mvn -pl protocols-service spring-boot:run
```

La base de cada servicio se crea automáticamente. En ejecución local, configura
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` según tu instalación; en Docker
Compose, los puertos MySQL del host son 3307 (roles), 3308 (usuarios) y 3309 (protocolos).

## Ejecutar con Docker Compose

Desde esta carpeta, prepara contraseñas locales:

```powershell
Copy-Item .env.example .env
```

Reemplaza los valores de `.env` por contraseñas locales y luego ejecuta:

```shell
docker compose up --build
```

Las APIs quedan disponibles en `http://localhost:8081`, `http://localhost:8082` y
`http://localhost:8083`.
Para detenerlas:

```shell
docker compose down
```

Los datos persisten en volúmenes separados. Para borrarlos intencionalmente:
`docker compose down -v`.

## APIs

Los errores siguen el formato JSON:
`{"status": ..., "error": ..., "message": ..., "path": ..., "timestamp": ...}`.

La documentación interactiva OpenAPI/Swagger de roles está en
`http://localhost:8081/swagger-ui.html`; su documento JSON se publica en
`http://localhost:8081/api-docs`.

### roles-service — `http://localhost:8081`

Swagger UI: `/swagger-ui.html`; OpenAPI JSON: `/api-docs`.

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/roles` | Roles activos |
| GET | `/api/roles/{id}` | Rol activo o 404 |
| POST | `/api/roles` | Crear rol, 201; 409 si el nombre ya existe |
| PUT | `/api/roles/{id}` | Actualizar rol |
| DELETE | `/api/roles/{id}` | Desactivar rol (borrado lógico) |

### users-service — `http://localhost:8082`

Swagger UI: `/swagger-ui.html`; OpenAPI JSON: `/api-docs`.

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/usuarios?buscar=texto` | Lista usuarios activos; filtro opcional |
| GET | `/api/usuarios/{id}` | Usuario activo o 404 |
| GET | `/api/usuarios/by-email/{correo}` | Usuario activo por correo |
| POST | `/api/usuarios` | Registro pendiente de aprobación, 201 |
| POST | `/api/usuarios/login` | Validar correo y contraseña |
| PUT | `/api/usuarios/{id}` | Actualizar perfil |
| PUT | `/api/usuarios/{id}/rol` | Cambiar rol, validado con `roles-service` |
| PATCH | `/api/usuarios/{id}/aprobacion` | Aprobar o rechazar cuenta |
| PUT | `/api/usuarios/{id}/contrasena` | Cambiar contraseña con contraseña actual |
| DELETE | `/api/usuarios/{id}` | Desactivar cuenta |

El registro acepta los campos de la app (`nombre`, `correo`, `contrasena`, `rut`,
`telefono`, `cargo`). El servidor asigna siempre el rol `Técnico`, que resuelve mediante
`WebClient`; así una cuenta nueva no puede elevar sus privilegios enviando otro rol. Las
nuevas cuentas empiezan pendientes de aprobación. Se admiten contraseñas de 6 a 72
caracteres, compatibles con el límite de 72 bytes de BCrypt.

```json
{
  "nombre": "Ana Torres",
  "correo": "ana@induztek.cl",
  "contrasena": "Clave123!",
  "rut": "12345678-5",
  "telefono": "+56 9 1234 5678",
  "cargo": "Técnico de Terreno"
}
```

Login responde el estado de aprobación en `aprobado`; la app debe permitir continuar
solo si ese valor es `true`. Las contraseñas se guardan con BCrypt y nunca aparecen en
respuestas.

La cobertura de los tres servicios se verifica con JaCoCo: `mvn verify` falla si la
cobertura de líneas de alguno queda bajo el 80 %. Los informes se generan en
`roles-service/target/site/jacoco/index.html`, `users-service/target/site/jacoco/index.html`
y `protocols-service/target/site/jacoco/index.html`.

### protocols-service — `http://localhost:8083`

Swagger UI: `/swagger-ui.html`; OpenAPI JSON: `/api-docs`.

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/protocolos?correoUsuario=ana@induztek.cl` | Protocolos del correo indicado, más recientes primero |
| GET | `/api/protocolos/{id}?correoUsuario=ana@induztek.cl` | Protocolo del usuario indicado o 404 |
| POST | `/api/protocolos` | Crear protocolo para una cuenta aprobada, 201 |
| PUT | `/api/protocolos/{id}?correoUsuario=ana@induztek.cl` | Actualizar protocolo propio |
| DELETE | `/api/protocolos/{id}?correoUsuario=ana@induztek.cl` | Eliminar protocolo propio, 204 |

El cuerpo conserva los nombres de campos de la app (`equipo`, `fecha`, `estado`,
`observaciones`, `correoUsuario`, `temperatura`, `humedad`). La fecha debe tener formato
`dd/MM/yyyy`; los estados permitidos son `Borrador`, `Registrado en terreno`, `Observado`,
`Formalizado` y `Anulado`. El correo se valida contra `users-service` al crear el protocolo
y no puede cambiarse al editarlo.

```json
{
  "equipo": "TR-01",
  "fecha": "23/11/2026",
  "estado": "Registrado en terreno",
  "observaciones": "Inspección preventiva",
  "correoUsuario": "ana@induztek.cl",
  "temperatura": "22.5",
  "humedad": "45"
}
```

## Integración móvil y seguridad pendiente

En un emulador Android, configura Retrofit con `http://10.0.2.2:8082/` para usuarios y
`http://10.0.2.2:8083/` para protocolos; desde un teléfono en la misma Wi-Fi, usa la IP
local del computador. Como la app y el servicio son HTTP, Android debe permitir tráfico
sin cifrar para desarrollo.

Los endpoints todavía no tienen autenticación ni autorización. El parámetro
`correoUsuario` delimita consultas, pero no verifica la identidad del solicitante. No
expongas los servicios a Internet hasta incorporar tokens y control de acceso. La
integración Retrofit completa, API Gateway y Eureka se abordarán en una etapa posterior.
