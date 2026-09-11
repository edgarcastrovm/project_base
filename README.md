# Final01 - Template inicial

Proyecto base compuesto por dos módulos independientes:

- **backend/**: API REST en Java 17 + Spring Boot 3 + PostgreSQL, con login JWT, respuesta estandarizada, DTOs, Lombok, interceptor de `requestId` y logging con Log4j2.
- **frontend/**: sitio web en Node.js + Express + EJS, con login, landing page y CRUD de usuarios por peticiones asíncronas, sesión en `sessionStorage`.

## 1. Levantar PostgreSQL (Docker, versión 17)

```bash
docker compose up -d
```

Esto crea el contenedor `final01_postgres` con la base `final01_db` (usuario/clave `postgres`/`postgres`) en el puerto `5432`.

## 2. Backend (Spring Boot)

Las credenciales de base de datos se leen de variables de entorno (con valores por defecto si no se definen), configuradas en `src/main/resources/application.yml`:

```yaml
datasource:
  url: ${DB_URL:jdbc:postgresql://localhost:5434/final01_db}
  username: ${DB_USERNAME:postgres}
  password: ${DB_PASSWORD:yondaime}
```

| Variable      | Descripción                          | Valor por defecto                                  |
|---------------|---------------------------------------|-----------------------------------------------------|
| `DB_URL`      | URL JDBC de PostgreSQL                | `jdbc:postgresql://localhost:5434/final01_db`        |
| `DB_USERNAME` | Usuario de la base de datos           | `postgres`                                          |
| `DB_PASSWORD` | Password de la base de datos          | `yondaime`                                          |
| `JWT_SECRET`  | Clave para firmar el JWT (Base64)     | valor de ejemplo incluido en `application.yml`      |
| `JWT_EXPIRATION_MS` | Expiración del token en milisegundos | `86400000` (24h)                              |
| `CORS_ALLOWED_ORIGINS` | Origen(es) permitidos para CORS (el frontend) | `http://localhost:3000`                  |

> Ajusta los valores por defecto en `application.yml` según tu instancia local de PostgreSQL (puerto/usuario/clave), o sobrescríbelos con variables de entorno sin tocar el archivo.

### 2.1 Ejecutar desde línea de comandos

**Opción A — Maven (sin compilar el jar), usando los valores por defecto del yml:**

```bash
cd backend
mvn spring-boot:run
```

**Opción B — Maven, sobrescribiendo credenciales con variables de entorno:**

```bash
cd backend
export DB_URL="jdbc:postgresql://localhost:5432/final01_db"
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
mvn spring-boot:run
```

**Opción C — Compilar el jar y ejecutarlo con `java -jar`:**

```bash
cd backend
mvn clean package -DskipTests
DB_URL="jdbc:postgresql://localhost:5432/final01_db" \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
java -jar target/backend.jar
```

En Windows (PowerShell), las variables se definen antes del comando:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/final01_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
java -jar target/backend.jar
```

### 2.2 Ejecutar desde IntelliJ IDEA

1. Abre la carpeta `backend/` como proyecto Maven (IntelliJ detecta el `pom.xml` automáticamente; espera a que descargue las dependencias).
2. Ubica la clase `com.uisrael.backend.BackendApplication` (paquete raíz) y ejecútala una vez con el botón ▶ para que IntelliJ genere la Run Configuration automáticamente (o créala manualmente: **Run > Edit Configurations… > + > Application**, seleccionando esa clase como *Main class*).
3. En esa Run Configuration, ve a **Modify options > Environment variables** (o el campo *Environment variables* si ya está visible) y agrega, separadas por `;`:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/final01_db;DB_USERNAME=postgres;DB_PASSWORD=postgres
   ```
   Si no defines estas variables, IntelliJ usará los valores por defecto del `application.yml` (`localhost:5434`, `postgres`/`yondaime`).
4. Verifica que el **Project SDK** sea Java 17 (**File > Project Structure > Project**).
5. Ejecuta con ▶ (Run) o 🐞 (Debug). La consola de IntelliJ mostrará los logs de arranque y, una vez levantado, la API queda disponible en `http://localhost:8080`.

- Puerto: `8080`
- Al iniciar por primera vez, `DataSeeder` crea automáticamente:
  - Roles: `ADMIN`, `USER`
  - Usuario administrador: **usuario `admin` / clave `Admin123`**

### Endpoints principales

| Método | Endpoint              | Descripción                    | Auth |
|--------|------------------------|---------------------------------|------|
| POST   | `/api/auth/login`      | Login, retorna JWT              | No   |
| GET    | `/api/usuarios`        | Listar usuarios                 | Sí   |
| GET    | `/api/usuarios/{id}`   | Obtener usuario                 | Sí   |
| POST   | `/api/usuarios`        | Crear usuario                   | Sí   |
| PUT    | `/api/usuarios/{id}`   | Actualizar usuario               | Sí   |
| GET    | `/api/roles`           | Listar roles                    | Sí   |
| POST   | `/api/roles`           | Crear rol                       | Sí   |
| PUT    | `/api/roles/{id}`      | Actualizar rol                  | Sí   |

Todas las respuestas siguen el formato estandarizado:

```json
{ "code": 200, "msg": "mensaje", "data": {}, "error": null }
```

Cada request recibe/propaga el header `X-Request-Id`, mapeado al MDC de Log4j2 (`%X{requestId}`) para trazar toda una transacción en los logs (`backend/logs/backend.log`).

## 3. Frontend (Node + EJS)

```bash
cd frontend
cp .env.example .env   # ajustar si el backend no corre en localhost:8080
npm install
npm start
```

- Puerto: `3000`
- Páginas: `/login`, `/home`, `/usuarios`
- El token JWT y los datos del usuario logueado se guardan en `sessionStorage` (clave `final01_session`); cada fetch al backend agrega `Authorization: Bearer <token>` y un `X-Request-Id` generado en el cliente.
- Paleta de colores: grises / blanco roto / azules, con topmenu de navegación e información del usuario logueado.

## Entidades

- `tbl_rol`: `id_rol`, `nombre`, `descripcion`, `activo`, `fecha_creacion`
- `tbl_user`: `id_user`, `username`, `password` (BCrypt), `email`, `nombres`, `apellidos`, `activo`, `fecha_creacion`, `fecha_actualizacion`, `id_rol` (FK)

## Notas

- Este template está pensado como punto de partida: agregar nuevos módulos backend siguiendo el patrón `entity -> repository -> dto -> service -> controller`, y nuevas páginas frontend siguiendo el patrón `views/*.ejs` + `public/js/*.js` + ruta en `routes/pages.js`.
- Cambiar `app.jwt.secret` en `application.yml` (o variable de entorno `JWT_SECRET`) antes de pasar a un ambiente real.
