# Manual de consumo de la API — Final01

Backend: `http://localhost:8080` (ajustar según despliegue). Todas las rutas cuelgan de `/api`.

## 1. Formato estándar de respuesta

Toda respuesta (éxito o error) sigue esta forma:

```json
{
  "code": 200,
  "msg": "mensaje descriptivo",
  "data": {},
  "error": null
}
```

| Campo   | Presente cuando                              |
|---------|-----------------------------------------------|
| `code`  | Siempre. Código HTTP de la operación.          |
| `msg`   | Siempre. Mensaje legible sobre el resultado.   |
| `data`  | Solo en respuestas exitosas (puede ser objeto, lista o `null`). |
| `error` | Solo en respuestas de error, con el detalle.   |

## 2. Headers

| Header           | Obligatorio | Descripción                                                                 |
|-------------------|:-----------:|-------------------------------------------------------------------------------|
| `Content-Type`     | Sí (con body) | `application/json`                                                           |
| `Authorization`    | Sí, excepto `/api/auth/**` | `Bearer <token>` obtenido en el login                          |
| `X-Request-Id`     | No          | Si no se envía, el backend genera uno. Se devuelve en la respuesta y se mapea al MDC de Log4j2 para rastrear la transacción en los logs (`backend/logs/backend.log`). |

## 3. Autenticación

### POST `/api/auth/login`

Endpoint público. Retorna el JWT a usar en el resto de endpoints.

**Request**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}'
```

**Response 200**

```json
{
  "code": 200,
  "msg": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "idUser": 1,
    "username": "admin",
    "email": "admin@uisrael.local",
    "nombreCompleto": "Administrador Sistema",
    "rol": "ADMIN"
  }
}
```

**Response 401** (credenciales inválidas)

```json
{ "code": 401, "msg": "Credenciales inválidas", "error": "Usuario o password incorrectos" }
```

> Usuario semilla creado por `DataSeeder` al primer arranque: `admin` / `Admin123`.

Guardar el token en variable de entorno para los siguientes ejemplos:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

## 4. Usuarios (`tbl_user`)

Todas requieren `Authorization: Bearer $TOKEN`.

### GET `/api/usuarios` — listar

```bash
curl -s http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer $TOKEN"
```

### GET `/api/usuarios/{id}` — obtener uno

```bash
curl -s http://localhost:8080/api/usuarios/1 \
  -H "Authorization: Bearer $TOKEN"
```

### POST `/api/usuarios` — crear

```bash
curl -s -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "jperez",
    "password": "Clave123",
    "email": "jperez@test.com",
    "nombres": "Juan",
    "apellidos": "Perez",
    "idRol": 2
  }'
```

Respuesta `201`, `data` con el `UserDTO` creado. Errores posibles: `400` (validación), `409` (username/email duplicado), `404` (idRol inexistente).

### PUT `/api/usuarios/{id}` — actualizar

```bash
curl -s -X PUT http://localhost:8080/api/usuarios/2 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "email": "jperez2@test.com",
    "nombres": "Juan Carlos",
    "apellidos": "Perez",
    "idRol": 2,
    "activo": true
  }'
```

`password` es opcional en el update: si se omite o va vacío, se conserva la contraseña actual.

## 5. Roles (`tbl_rol`)

Mismo patrón que usuarios, todas requieren token.

### GET `/api/roles`

```bash
curl -s http://localhost:8080/api/roles -H "Authorization: Bearer $TOKEN"
```

### POST `/api/roles`

```bash
curl -s -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nombre": "SUPERVISOR", "descripcion": "Supervisor de área"}'
```

### PUT `/api/roles/{id}`

```bash
curl -s -X PUT http://localhost:8080/api/roles/3 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nombre": "SUPERVISOR", "descripcion": "Actualizado", "activo": true}'
```

## 6. Personas (`tbl_persona`) — ⚠️ EN CONSTRUCCIÓN

Entidad y DTO ya creados (`TblPersona`, `TblPersonaDto`), pero el endpoint **está incompleto**: no tiene repository ni service, por lo que siempre responde `data: null` sin consultar la base de datos.

### GET `/api/persona` — listar (incompleto)

```bash
curl -s http://localhost:8080/api/persona \
  -H "Authorization: Bearer $TOKEN"
```

**Response actual**

```json
{ "code": 200, "msg": "Personas listadas Correctamente", "data": null }
```

### Pendiente para completar el módulo

Siguiendo el mismo patrón usado en `UserController` / `RolController`:

1. `PersonaRepository extends JpaRepository<TblPersona, Integer>`.
2. `PersonaService` (interfaz) + `PersonaServiceImpl` con `listar()`, `crear()`, `actualizar()`.
3. Mapear `TblPersona` → `TblPersonaDto` (hoy el DTO expone la entidad `TblUser` completa; conviene exponer solo `idUser`/`username` para no filtrar el `password`).
4. Cambiar el `@GetMapping` de `PersonaController` para devolver `List<TblPersonaDto>` en vez de `TblPersonaDto` único, y agregar `@PostMapping`/`@PutMapping` para crear/actualizar.
5. Ajustar la ruta a `/api/personas` (plural) para mantener consistencia con `/api/usuarios` y `/api/roles`.

## 7. Errores comunes

| Código | Causa típica                                        |
|--------|------------------------------------------------------|
| 400    | Body inválido (`MethodArgumentNotValidException`)     |
| 401    | Sin token, token expirado o credenciales inválidas    |
| 403    | Token válido pero sin permisos / origen CORS no permitido |
| 404    | Recurso no encontrado (`ApiException.notFound`)       |
| 409    | Conflicto: username/email/rol duplicado               |
| 500    | Error no controlado                                    |

Ejemplo de error de validación:

```bash
curl -s -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"username":"x","password":"123","email":"no-es-email","idRol":2}'
```

```json
{
  "code": 400,
  "msg": "Datos de entrada inválidos",
  "error": "email: El email no tiene un formato válido | username: El username debe tener entre 3 y 50 caracteres | password: El password debe tener al menos 6 caracteres"
}
```
