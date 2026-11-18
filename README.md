# MHM Store API — APF2 (Persistencia + Seguridad + Dominio Completo)

API REST desarrollada con **Spring Boot 3**, **Spring Data JPA / Hibernate**, **MySQL** y **Spring Security + JWT** para la gestión integral de inventario, carritos, pedidos, reportes de ventas y administración de usuarios con permisos granulares en **MHM Store**.

---

## Modelo de Dominio y Base de Datos Relacional

El sistema implementa todas las clases y relaciones especificadas en el diagrama UML del proyecto:

1. **`Usuario`** (`usuarios`) + **`Rol`** (`ADMIN`, `TRABAJADOR`) + **`Permiso`** (`usuario_permisos`):
   - Autenticación con contraseñas encriptadas mediante **BCrypt** y tokens **JWT**.
   - El **Administrador (`ADMIN`)** tiene acceso a todos los campos y módulos del sistema, puede crear nuevos trabajadores y seleccionar qué permisos específicos (`VER_CATALOGOS`, `GESTIONAR_PRODUCTOS`, `GESTIONAR_INVENTARIO`, `GESTIONAR_CATALOGOS`, `GESTIONAR_CARRITO`, `GESTIONAR_PEDIDOS`, `VER_REPORTES`) tiene cada trabajador.
2. **`Categoria`** (`categorias`): clasificación de mochilas.
3. **`Almacen`** (`almacenes`): ubicaciones físicas de almacenamiento.
4. **`Producto`** (`productos`): mochilas con SKU único (`codigo`), `modelo`, `color`, `precio`, `cantidad` (stock), `descripcion`, `categoria` y `almacen`.
5. **`Carrito`** (`carritos`) + **`EstadoCarrito`** (`ACTIVO`, `ENVIADO_A_CAJA`, `CANCELADO`) + **`ItemCarrito`** (`items_carrito`):
   - Permite a un usuario gestionar ítems, calcular subtotales/totales y enviar el carrito a caja.
6. **`Pedido`** (`pedidos`) + **`EstadoPedido`** (`PENDIENTE`, `PROCESADO`, `COMPLETADO`):
   - Generado de forma **transaccional** a partir de un `Carrito`, verificando y descontando automáticamente el stock de cada producto.
7. **`ReporteVentas`** (`reportes_ventas`) + **`DetalleReporte`** (`detalles_reporte`):
   - Generado mediante consultas **JPQL** por rango de fechas o por mes/año, calculando el total de productos vendidos e ingresos detallados por producto.

---

## Cumplimiento de Requisitos APF2 — Semana 10

| Requisito | Implementación en el proyecto |
| :--- | :--- |
| **Migración a BD relacional** | Esquema versionado con Flyway; Hibernate valida el esquema al iniciar (`ddl-auto=validate`). |
| **CRUD persistente** | Repositorios JPA y controladores REST completos para `Usuario`, `Producto`, `Categoria`, `Almacen`, `Carrito`, `Pedido` y `ReporteVentas`. |
| **Consulta JPQL útil** | 1) `ProductoRepository.buscarProductosConStockBajo(:limite)` (`GET /api/v1/productos/stock-bajo`)<br>2) `PedidoRepository.buscarPedidosPorRangoFechas(:fechaInicio, :fechaFin)` para generación de reportes de ventas. |
| **Operación transaccional** | 1) `InventarioServiceImpl` (`@Transactional`): entradas y salidas de stock atómicas.<br>2) `PedidoServiceImpl.crearDesdeCarrito` (`@Transactional`): validación y descuento atómico de stock de múltiples ítems al confirmar un pedido. |
| **Spring Security + Permisos** | Roles `ADMIN` y `TRABAJADOR` + permisos granulares asignables por trabajador (`SecurityConfig`). |
| **Autenticación JWT** | `POST /api/v1/auth/login`, `JwtService`, `JwtAuthenticationFilter` stateless. |
| **Tests actualizados** | Pruebas unitarias y de integración con H2 en memoria y `@WithMockUser` (`ProductoControllerTest`, `UsuarioControllerSecurityTest`, `CarritoPedidoReporteIntegrationTest`, `ProductoRepositoryIntegrationTest`, `InventarioServiceImplTest`, `ProductoServiceImplTest`). |
| **Postman actualizado** | Colección `postman/MHM Store API - APF2.postman_collection` con login JWT, gestión de permisos de trabajadores, CRUDs, inventario transaccional, carritos, pedidos y reportes. |

---

## Endpoints Principales

### Configuración segura

Configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET_BASE64` en el entorno antes de iniciar el backend. La clave JWT debe ser Base64 y decodificar a por lo menos 32 bytes. Para crear el primer administrador, configura `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`; el registro público está deshabilitado y las cuentas posteriores se administran desde `/api/v1/usuarios`. Para despliegues, define también `CORS_ALLOWED_ORIGINS` con los orígenes exactos separados por coma.

Flyway crea el esquema inicial y `ddl-auto=validate` detecta diferencias sin modificar tablas automáticamente. En bases ya existentes, Flyway usa baseline versión 0 y ejecuta la migración idempotente inicial.

### 1. Autenticación (`/api/v1/auth`)
- `POST /api/v1/auth/login` — Público. Devuelve `token` JWT, `email`, `rol` y `permisos`.

### 2. Usuarios y Permisos (`/api/v1/usuarios`) — Exclusivo `ADMIN`
- `GET /api/v1/usuarios` — Lista todos los usuarios y sus permisos.
- `GET /api/v1/usuarios/permisos` — Lista todos los permisos seleccionables del sistema.
- `GET /api/v1/usuarios/{id}` — Obtiene un usuario por ID.
- `POST /api/v1/usuarios` — Crea un nuevo usuario (`ADMIN` o `TRABAJADOR`) seleccionando su lista de permisos.
- `PUT /api/v1/usuarios/{id}` — Actualiza todos los campos de un usuario (nombre, email, contraseña, rol, permisos).
- `PATCH /api/v1/usuarios/{id}/permisos` — Actualiza específicamente los permisos asignados a un trabajador.
- `DELETE /api/v1/usuarios/{id}` — Elimina un usuario.

### 3. Productos (`/api/v1/productos`), Categorías (`/api/v1/categorias`) y Almacenes (`/api/v1/almacenes`)
- `GET /api/v1/productos` — Lista productos con filtros opcionales (`almacen`, `disponible`, `stockBajo`, `q`).
- `GET /api/v1/productos/stock-bajo?limite=5` — Consulta JPQL de productos con stock bajo.
- `GET /api/v1/productos/{id}` — Obtiene producto por ID.
- `POST`, `PUT`, `PATCH`, `DELETE /api/v1/productos/**` — Requiere `ADMIN` o permiso `GESTIONAR_PRODUCTOS`.
- `GET`, `POST`, `PUT`, `DELETE /api/v1/categorias/**` y `/api/v1/almacenes/**` — Lectura general; escritura requiere `ADMIN` o permiso `GESTIONAR_CATALOGOS`.

### 4. Inventario Transaccional (`/api/v1/inventario`)
- `PATCH /api/v1/inventario/{productoId}/salida` — Descuenta stock de forma transaccional (`ADMIN` o `GESTIONAR_INVENTARIO`).
- `PATCH /api/v1/inventario/{productoId}/entrada` — Incrementa stock de forma transaccional (`ADMIN` o `GESTIONAR_INVENTARIO`).

### 5. Carrito de Compras (`/api/v1/carritos`)
- `GET /api/v1/carritos/mi-carrito` — Obtiene o crea el carrito del usuario autenticado.
- `GET /api/v1/carritos/usuario/{usuarioId}` — Obtiene o crea el carrito activo del usuario.
- `POST /api/v1/carritos` — Crea un nuevo carrito (`{"usuarioId": 1}`).
- `POST /api/v1/carritos/{id}/items` — Agrega un producto y cantidad al carrito.
- `DELETE /api/v1/carritos/{id}/items/{productoId}` — Quita un producto del carrito.
- `DELETE /api/v1/carritos/{id}/items` — Vacía el carrito.
- `PATCH /api/v1/carritos/{id}/enviar-a-caja` — Cambia el estado del carrito a `ENVIADO_A_CAJA`.

### 6. Pedidos (`/api/v1/pedidos`)
- `POST /api/v1/pedidos` — Operación transaccional: crea un pedido desde un carrito (`{"carritoId": 1}`), descontando el stock de cada producto.
- `GET /api/v1/pedidos` — Lista todos los pedidos registrados.
- `GET /api/v1/pedidos/{id}` — Obtiene el detalle de un pedido.
- `PATCH /api/v1/pedidos/{id}/estado` — Actualiza el estado del pedido (`PENDIENTE`, `PROCESADO`, `COMPLETADO`).

### 7. Reportes de Ventas (`/api/v1/reportes`)
- `POST /api/v1/reportes/generar?fechaInicio=2026-11-01T00:00:00&fechaFin=2026-11-30T23:59:59` — Genera y guarda un reporte de ventas por rango de fechas usando JPQL (`ADMIN` o `VER_REPORTES`).
- `POST /api/v1/reportes/mensual?mes=11&anio=2026` — Genera y guarda el reporte mensual de ventas.
- `GET /api/v1/reportes` — Lista los reportes generados.
- `GET /api/v1/reportes/{id}` — Obtiene un reporte con su lista de `DetalleReporte` por producto.
