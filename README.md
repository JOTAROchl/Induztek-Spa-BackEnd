# Induztek SpA — primer microservicio

En esta etapa el backend contiene solamente `roles-service`, basado en la estructura de
`MiRegistro_Microservicios`. Los servicios de usuarios y protocolos se incorporarán en
etapas posteriores, cuando se soliciten.

## Arquitectura actual

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---:|---|---|
| `roles-service` | 8081 | `induztek_roles` | Catálogo de roles |

El servicio mantiene su propia base de datos MySQL y no comparte datos con otros
servicios. En una etapa posterior, `users-service` podrá consultar este catálogo mediante
`WebClient`, sin conectarse directamente a esta base.

## Requisitos

- JDK 21
- Maven 3.9+
- Docker Desktop con Docker Compose

## Ejecutar con Docker Compose

Desde esta carpeta, prepara contraseñas locales:

```powershell
Copy-Item .env.example .env
```

Reemplaza los valores de `.env` por contraseñas locales y luego ejecuta:

```shell
docker compose up --build
```

La API queda disponible en `http://localhost:8081`. Para detenerla:

```shell
docker compose down
```

Los datos persisten en un volumen exclusivo. Para borrarlos intencionalmente:
`docker compose down -v`.

## Ejecución local

```shell
docker compose up -d roles-db
mvn -pl roles-service spring-boot:run
```

La configuración local usa MySQL en `localhost:3307`. Se puede ajustar mediante las
variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD`.

## API

Los errores siguen el formato JSON:
`{"status": ..., "error": ..., "message": ..., "path": ..., "timestamp": ...}`.

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/roles` | Lista de roles activos |
| GET | `/api/roles/{id}` | Rol activo o 404 |
| POST | `/api/roles` | Crear rol, 201; 409 si el nombre ya existe |
| PUT | `/api/roles/{id}` | Actualizar rol |
| DELETE | `/api/roles/{id}` | Desactivar rol sin borrar sus datos |

Ejemplo para crear un rol:

```json
{
  "nombre": "Técnico",
  "descripcion": "Registro de inspecciones en terreno"
}
```

Al iniciar por primera vez se agregan los roles Administrador, Jefe técnico, Ingeniero,
Técnico y Cliente. El proceso es idempotente y no sobrescribe cambios existentes.
