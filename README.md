# MiniMarket DSY · Backend (API REST)

Backend de la tienda MiniMarket DSY para **DSY1104 – Evaluación Parcial 3**. API REST con
**Spring Boot 4.1 (Java 21)**, **MySQL 8.4**, **Spring Security + JWT** y documentación con **Swagger**.

El frontend (React) está en el repositorio `tienda-conveniencia-react`.

## Requisitos

- Java 21
- MySQL 8.x en `localhost:3306`
- No hace falta instalar Maven: el proyecto incluye el Maven Wrapper (`mvnw`).

## Puesta en marcha

1. Crear la base de datos y el usuario de la aplicación (una sola vez):

   ```sql
   CREATE DATABASE tienda_conveniencia CHARACTER SET utf8mb4;
   CREATE USER 'tienda'@'localhost' IDENTIFIED BY 'Tienda2026!';
   GRANT ALL PRIVILEGES ON tienda_conveniencia.* TO 'tienda'@'localhost';
   ```

2. Iniciar el backend:

   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```

   Al primer inicio Hibernate crea las tablas y se cargan datos de ejemplo
   (4 categorías, 10 productos, 3 usuarios y 3 boletas).

3. Abrir la documentación: **http://localhost:8080/swagger-ui.html**

### Configuración

Todos los valores tienen un valor por defecto para desarrollo y se pueden cambiar con variables de entorno:

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/tienda_conveniencia` | Conexión a MySQL |
| `DB_USER` / `DB_PASSWORD` | `tienda` / `Tienda2026!` | Credenciales de la base |
| `JWT_SECRETO` | clave de desarrollo | Clave HMAC (Base64, ≥ 256 bits) para firmar los JWT. **Cambiar en producción.** |
| `CORS_ORIGENES` | `http://localhost:5173,http://localhost:4173` | Orígenes del frontend autorizados |

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@duoc.cl | admin123 |
| Vendedor | vendedor@profesor.duoc.cl | vende123 |
| Cliente | juan.perez@gmail.com | juan1234 |

## Modelo de datos

`USUARIO ⇢ BOLETA → DETALLE_BOLETA ← PRODUCTO ← CATEGORIA` (esquema SQL en [`bd/esquema.sql`](bd/esquema.sql)).

- Una **boleta** puede tener usuario (compra con sesión) o no (compra como invitado).
- El **detalle** guarda nombre y precio cobrado, para que la boleta no cambie si el producto se edita o se elimina.

## Arquitectura

```
cl.duoc.tienda
├── modelo/        Entidades JPA y enumeraciones
├── repositorio/   Interfaces Spring Data JPA
├── dto/           Contrato JSON de la API (records con Bean Validation)
├── servicio/      Lógica de negocio (compra, stock, reportes, usuarios)
├── controlador/   Controladores REST /api/v1 con anotaciones OpenAPI
├── seguridad/     Spring Security, JWT, filtro y respuestas 401/403
├── validacion/    Validadores propios (RUN, dominios de correo)
├── excepcion/     Excepciones de negocio y manejador global de errores
└── config/        Swagger y datos iniciales
```

## Endpoints (`/api/v1`)

| Recurso | Endpoints | Acceso |
|---|---|---|
| Autenticación | `POST /auth/login`, `/auth/registro`, `/auth/refresh`, `/auth/logout` · `GET /auth/me` | Público (`/me` con sesión) |
| Productos | `GET /productos`, `/productos/{id}` | Público |
| | `POST /productos` · `PUT/DELETE /productos/{id}` · `PATCH /productos/{id}/stock` · `GET /productos/criticos` | Administrador |
| Categorías | `GET /categorias`, `/categorias/{id}` | Público |
| | `POST /categorias` · `PUT/DELETE /categorias/{id}` | Administrador |
| Boletas | `POST /boletas` (comprar) | Público (con o sin sesión) |
| | `GET /boletas`, `/boletas/{id}` | Administrador, Vendedor (cliente: solo las suyas) |
| | `GET /boletas/mias` | Cualquier usuario con sesión |
| | `PATCH /boletas/{id}/estado` | Administrador |
| Usuarios | `GET/POST /usuarios` · `GET/PUT/DELETE /usuarios/{id}` · `GET /usuarios/{id}/boletas` | Administrador |
| Perfil | `GET/PUT /perfil` | Cualquier usuario con sesión |
| Reportes | `GET /reportes/resumen` | Administrador |

Todas las respuestas de error usan el mismo formato:

```json
{ "fecha": "...", "estado": 409, "error": "Conflict", "mensaje": "Ya existe un producto con el código BEB-001.", "ruta": "/api/v1/productos", "errores": null }
```

## Seguridad

- **Contraseñas** con BCrypt.
- **Token de acceso** JWT (HS384, 15 minutos) en el header `Authorization: Bearer <token>`.
- **Token de refresco** JWT (7 días) en una cookie `HttpOnly`, `SameSite=Strict`, que solo viaja a `/api/v1/auth`.
  `POST /auth/refresh` entrega un nuevo token de acceso y renueva la cookie (rotación).
- El claim `tipo` impide usar un token de refresco como token de acceso.
- En cada petición el filtro JWT lee el usuario desde la base: si fue desactivado o cambió de rol, aplica de inmediato.
- Permisos por ruta en `ConfiguracionSeguridad` y CORS restringido al frontend.

## Pruebas

```bash
./mvnw test
```

35 pruebas (JUnit 5, AssertJ, MockMvc) sobre una base H2 en memoria: validaciones, JWT, lógica de compra
y la API completa (login, refresh, permisos por rol, CRUD y reglas de negocio).
