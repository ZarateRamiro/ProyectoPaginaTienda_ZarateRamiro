# Tienda Web Proyecto

Aplicación web de gestión de tienda desarrollada en **Java 17**, **Jakarta Servlet/JSP**, **JDBC**, **DAO** y **MySQL**.

El sistema evoluciona el proyecto del año anterior incorporando carrito de compras, control de stock, pool de conexiones, contenedorización y CI.

---

## Características principales

- Login con roles **ADMIN** y **USUARIO**
- CRUD de productos con categoría, stock e imagen por URL
- CRUD de usuarios (solo administrador)
- Carrito de compras con validación de stock y finalización de compra
- Relaciones entre entidades: `Usuario` → `Carrito` → `ItemCarrito` → `Producto` → `Categoria`
- Pool de conexiones con **HikariCP**
- Manejo de excepciones de negocio (`SinStockException`)
- Uso de **Streams** y colecciones en la capa de servicio
- Tests unitarios con **JUnit 5**
- **Docker**, **docker-compose** y **GitHub Actions**

---

## Requisitos

- JDK 17
- Maven 3.9+
- MySQL 8 (local o vía Docker)
- Tomcat 10+ (local o vía Docker)

---

## Base de datos

### Opción 1: Docker (recomendada)

```bash
docker compose up --build
```

Esto levanta:

- MySQL en `localhost:3307`
- Tomcat en `http://localhost:8080`

El script `src/main/resources/bd/Tienda.sql` se carga automáticamente al iniciar MySQL.

### Opción 2: MySQL local

1. Ejecutar `src/main/resources/bd/Tienda.sql` en MySQL.
2. Ajustar `src/main/resources/database.properties` si hace falta.

Credenciales de prueba:

| Rol | Usuario | Contraseña |
|-----|---------|------------|
| Admin | RamiroAdmin | admin |
| Usuario | usuario | 1234 |

---

## Ejecución local (sin Docker)

```bash
mvn clean package
```

Desplegar `target/tiendaWebProyecto.war` en Tomcat y acceder a `http://localhost:8080/tiendaWebProyecto/`.

---

## Tests

```bash
mvn test
```

Incluye pruebas unitarias de entidades, utilidades y lógica del carrito.

---

## CI/CD

El workflow `.github/workflows/ci.yml` ejecuta en cada push/PR:

1. `mvn verify` (compilación + tests)
2. Build del WAR
3. Build de la imagen Docker

---

## Estructura del proyecto

```
src/main/java/org/isp63/prog1/
├── dao/          # Acceso a datos JDBC
├── entities/     # Modelo orientado a objetos
├── exception/    # Excepciones de negocio
├── interfaces/   # DAO y conexión
├── service/      # Lógica de negocio (carrito, streams)
├── servlets/     # Controladores web
└── util/         # Pool, roles, utilidades

src/main/webapp/  # Vistas JSP
src/main/resources/bd/  # Script SQL
```

---

## Decisiones de diseño

- **Categoria como entidad separada**: cumple el requisito de entidades relacionadas y permite filtrar productos por categoría.
- **Stock**: se valida al agregar/actualizar carrito y se descuenta al finalizar la compra dentro de una transacción.
- **Carrito activo**: cada usuario tiene un carrito `ACTIVO`; al finalizar pasa a `FINALIZADO` y se crea uno nuevo en la próxima compra.

---

## Pendientes opcionales

- Hashear contraseñas con `PasswordUtil` (BCrypt ya está en el proyecto)
- CRUD de categorías desde el panel admin
- Tests de integración contra MySQL con Testcontainers
