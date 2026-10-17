# MHM Store API

API REST desarrollada con **Spring Boot** para gestionar las mochilas de MHM Store. Permite registrar, consultar, actualizar y eliminar productos utilizando una arquitectura por capas.

## Requisitos

Para ejecutar el proyecto se necesita:

* Java 17
* Maven o Maven Wrapper incluido en el proyecto
* NetBeans IDE
* Postman para realizar las pruebas de los endpoints

## Ejecutar el proyecto

### Desde NetBeans

1. Abrir el proyecto `mhm-store-api` en NetBeans.
2. Esperar a que Maven descargue las dependencias.
3. Ubicar la clase principal `MhmStoreApiApplication`.
4. Ejecutar el proyecto con **Run Project**.
5. Verificar en la consola que Spring Boot se haya iniciado correctamente.

La API estará disponible en:

`http://localhost:8080`

El recurso principal se encuentra en:

`http://localhost:8080/api/v1/productos`

### Desde la terminal

En Windows, ubicarse en la carpeta raíz del proyecto y ejecutar:

`mvnw.cmd spring-boot:run`

Para detener la aplicación se puede utilizar `Ctrl + C`.

## Probar la API

La API puede probarse utilizando **Postman**.

Los principales endpoints implementados son:

| Método | Endpoint                 | Descripción                        |
| ------ | ------------------------ | ---------------------------------- |
| GET    | `/api/v1/productos`      | Lista todos los productos          |
| GET    | `/api/v1/productos/{id}` | Obtiene un producto por ID         |
| POST   | `/api/v1/productos`      | Registra una nueva mochila         |
| PUT    | `/api/v1/productos/{id}` | Reemplaza los datos de una mochila |
| PATCH  | `/api/v1/productos/{id}` | Actualiza parcialmente una mochila |
| DELETE | `/api/v1/productos/{id}` | Elimina una mochila                |

Ejemplo de JSON para registrar una mochila:

```json
{
  "codigo": "MOCH001",
  "modelo": "Mochila Urbana",
  "color": "Negro",
  "categoria": "URBANA",
  "precio": 89.90,
  "cantidad": 10,
  "almacen": "PRINCIPAL"
}
```

La API también controla errores HTTP. Por ejemplo, consultar:

`GET /api/v1/productos/99999`

devuelve **404 Not Found** cuando el producto solicitado no existe.

También se utiliza **409 Conflict** cuando se intenta registrar un código de producto que ya existe.

## Ejecutar las pruebas automatizadas

El proyecto contiene pruebas automatizadas para las capas Service y Controller.

Desde NetBeans se pueden ejecutar utilizando:

**Test Project**

También pueden ejecutarse desde la terminal de Windows:

`mvnw.cmd test`

Se implementaron como mínimo las siguientes pruebas:

* `crear_debeNormalizarCodigoYGuardarProducto()`
* `crear_debeRechazarCodigoDuplicado()`
* `crearProducto_debeResponder201()`
* `obtenerProductoInexistente_debeResponder404()`

Las dos primeras verifican la lógica de negocio del Service y las dos últimas prueban el comportamiento HTTP mediante MockMvc.

## TDD: RED → GREEN → REFACTOR

Para demostrar el uso de TDD se utilizó el caso de código de producto duplicado.

**RED:** se planteó una prueba que verifica que el sistema debe rechazar la creación de un producto cuando su código ya está registrado.

**GREEN:** se implementó la validación del código en `ProductoServiceImpl`, lanzando `CodigoDuplicadoException` cuando el repositorio encuentra otro producto con el mismo código.

**REFACTOR:** la validación se centralizó en el método privado `validarCodigoDisponible(String codigo, Long idActual)`, permitiendo reutilizar la misma lógica en las operaciones de creación y actualización sin duplicar código.

Después de la refactorización, las pruebas continúan ejecutándose correctamente.

## Estructura del proyecto

El proyecto utiliza la siguiente organización:

```text
src/
├── main/
│   └── java/com/mhmstore/api/
│       ├── controller/
│       ├── dto/
│       ├── exception/
│       ├── model/
│       ├── repository/
│       ├── service/
│       └── MhmStoreApiApplication.java
│
└── test/
    └── java/com/mhmstore/api/
        ├── controller/
        └── service/
```

### Controller

Contiene `ProductoController` y los componentes encargados de recibir las solicitudes HTTP y devolver las respuestas correspondientes.

### Service

Contiene la lógica de negocio de la aplicación, incluyendo las validaciones relacionadas con los productos.

### Repository

Se encarga del acceso y almacenamiento de los productos. En esta versión se utiliza un repositorio **en memoria**, por lo que no se requiere una base de datos.

### Model

Contiene las entidades y enumeraciones utilizadas por el sistema, como `Producto`, `Categoria` y `Almacen`.

### DTO

Contiene los objetos utilizados para recibir y devolver información mediante la API.

### Exception

Contiene las excepciones personalizadas utilizadas para manejar situaciones como productos inexistentes o códigos duplicados.

## Arquitectura

La aplicación sigue una arquitectura por capas:

`Controller → Service → Repository`

Además, se utiliza **inyección de dependencias mediante constructor** para mantener las clases desacopladas y facilitar las pruebas automatizadas.

## Tecnologías utilizadas

* Java 17
* Spring Boot
* Spring Web MVC
* Bean Validation
* JUnit
* Mockito
* MockMvc
* Maven
* Postman

## Módulo de persistencia, productos y almacenes

Este módulo forma parte del sistema de inventario MHM STORE, desarrollado con Java y Spring Boot para gestionar los productos de una tienda de mochilas.

### Funcionalidades implementadas

- **Persistencia con JPA y MySQL:** almacenamiento de productos, categorías y almacenes en la base de datos.
- **Gestión de productos:** operaciones CRUD mediante los endpoints GET, POST, PUT, PATCH y DELETE.
- **Gestión de categorías y almacenes:** registro, consulta, actualización y eliminación de información.
- **Validaciones:** comprobación de campos obligatorios, longitudes, precios y cantidades.
- **Manejo de errores HTTP:** respuestas para solicitudes inválidas (400), recursos inexistentes (404) y conflictos de datos (409).
- **Filtros de productos:** búsqueda por almacén, disponibilidad, stock bajo y texto de búsqueda.
- **Documentación y pruebas:** comprobación de endpoints y respuestas mediante Swagger, además de pruebas automatizadas con Maven.

### Tecnologías utilizadas

Java, Spring Boot, Spring Data JPA, MySQL, Jakarta Validation, Maven, Swagger/OpenAPI y Git.

### Responsabilidad del módulo

Persistencia de datos, gestión de productos, categorías y almacenes, validaciones de entrada y tratamiento de errores de la API.


## Módulo de operación transaccional de inventario

Este módulo implementa el control seguro de existencias (entradas y salidas de stock) garantizando la atomicidad de las operaciones en la base de datos mediante `@Transactional`.

### Funcionalidades implementadas

- **Servicio de inventario (`InventarioService` e `InventarioServiceImpl`):** lógica de negocio anotada con `@Transactional` para registrar salidas y entradas de stock de forma atómica.
- **Validación de cantidades positivas:** rechazo inmediato (`400 Bad Request`) cuando la cantidad solicitada es nula, cero o negativa.
- **Control de stock disponible:** verificación previa de existencias antes de descontar stock, devolviendo `409 Conflict` cuando la cantidad solicitada supera el stock actual y evitando cambios parciales en la base de datos.
- **Controlador REST (`InventarioController`):** exposición de los endpoints transaccionales de inventario.
- **Pruebas automatizadas (`InventarioServiceImplTest`):** pruebas unitarias con JUnit 5 y Mockito para validar salidas exitosas, bloqueo por stock insuficiente sin modificar el producto y rechazo de cantidades inválidas.

### Endpoints de inventario

| Método | Endpoint | Descripción |
| ------ | -------- | ----------- |
| PATCH | `/api/v1/inventario/{productoId}/salida` | Descuenta stock de un producto de forma transaccional |
| PATCH | `/api/v1/inventario/{productoId}/entrada` | Incrementa stock de un producto de forma transaccional |

### Documentación de resultados en Postman

1. **Salida de stock válida (`200 OK`):**
   - **Petición:** `PATCH /api/v1/inventario/1/salida` con body `{"cantidad": 3}` (o parámetro `?cantidad=3`).
   - **Resultado:** Descuenta las unidades solicitadas y devuelve el `ProductoResponse` con la `cantidad` actualizada.
2. **Error por stock insuficiente (`409 Conflict`):**
   - **Petición:** `PATCH /api/v1/inventario/1/salida` con body `{"cantidad": 999}`.
   - **Resultado:** La operación falla de forma segura, `@Transactional` evita cambios parciales y el stock del producto permanece intacto.
3. **Error por cantidad no positiva (`400 Bad Request`):**
   - **Petición:** `PATCH /api/v1/inventario/1/salida` con body `{"cantidad": 0}`.
   - **Resultado:** Rechaza la solicitud antes de modificar el registro en la base de datos.
=======
### Consulta de productos con stock bajo mediante JPQL

Se implementó una consulta utilizando JPQL en `ProductoRepository` para identificar los productos cuyo stock disponible es menor o igual a un límite determinado.

**Endpoint:** `GET /api/v1/productos/stock-bajo?limite=5`

**Parámetro:**
- `limite`: cantidad máxima de unidades para considerar un producto con stock bajo. Si no se especifica, se utiliza el valor predeterminado de `5`.

**Funcionamiento de la consulta:**
- Filtra los productos cuya cantidad disponible sea menor o igual al límite indicado.
- Ordena los resultados de menor a mayor cantidad disponible.
- Devuelve una lista de objetos `ProductoResponse`.

**Implementación:** la consulta se encuentra en `ProductoRepository` y su funcionalidad se expone a través de `ProductoService`, `ProductoServiceImpl` y `ProductoController`.

**Ejemplo de solicitud:**

```http
GET http://localhost:8080/api/v1/productos/stock-bajo?limite=5
```

**Respuesta esperada:** HTTP `200 OK`, con una lista de productos que cumplen la condición. Si no existen productos que cumplan el límite, se devuelve una lista vacía.



---

## Seguridad JWT (integración de Ángeles)

Se agregó autenticación stateless con JWT, contraseñas BCrypt y los roles `ADMIN` y `TRABAJADOR`.

### 1. Requisitos y configuración

Antes de iniciar la aplicación, configura una clave JWT Base64 de al menos 32 bytes. No publiques la clave ni las contraseñas en Git.

**Git Bash:**

```bash
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
export BOOTSTRAP_ADMIN_EMAIL="admin@mhmstore.com"
export BOOTSTRAP_ADMIN_PASSWORD="CambiaEstaClavePorUnaSegura123!"
export BOOTSTRAP_WORKER_EMAIL="trabajador@mhmstore.com"
export BOOTSTRAP_WORKER_PASSWORD="CambiaEstaOtraClave123!"
./mvnw spring-boot:run
```

En Windows PowerShell, genera una clave compatible con Windows PowerShell/.NET así:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$env:BOOTSTRAP_ADMIN_EMAIL = "admin@mhmstore.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "CambiaEstaClavePorUnaSegura123!"
$env:BOOTSTRAP_WORKER_EMAIL = "trabajador@mhmstore.com"
$env:BOOTSTRAP_WORKER_PASSWORD = "CambiaEstaOtraClave123!"
./mvnw.cmd spring-boot:run
```

Las contraseñas para las cuentas nuevas deben tener al menos 12 caracteres y no pueden tener espacios al inicio ni al final.

El inicializador crea el administrador y/o trabajador solamente si se proporcionan las variables correspondientes y el correo todavía no existe. Las contraseñas se guardan con BCrypt. Una vez creadas las cuentas, borra las variables de bootstrap o déjalas vacías. Para ambientes reales, utiliza un gestor de secretos y un procedimiento controlado de alta de usuarios.

La aplicación mantiene la configuración MySQL existente (`mhm_store`) y Hibernate crea/actualiza la tabla `usuarios` con `ddl-auto=update`.

### 2. Iniciar sesión

`POST http://localhost:8080/api/v1/auth/login`

```json
{
  "email": "admin@mhmstore.com",
  "contrasena": "CambiaEstaClavePorUnaSegura123!"
}
```

La respuesta contiene `token`, `tipo` (`Bearer`), `email` y `rol`. En las siguientes solicitudes añade el encabezado:

```http
Authorization: Bearer PEGA_AQUI_EL_TOKEN
```

### 3. Reglas de autorización actuales

- `POST /api/v1/auth/login`: público.
- `POST /api/v1/usuarios`: requiere token JWT de `ADMIN`; crea un usuario con rol `ADMIN` o `TRABAJADOR`.
- `/api/v1/usuarios/**`: reservado a `ADMIN`.
- `GET /api/v1/productos/**` y otros GET de `/api/v1/**`: accesibles a `ADMIN` y `TRABAJADOR`.
- `POST`, `PUT`, `PATCH` y `DELETE` de `/api/v1/**`: requieren `ADMIN`.
- Swagger UI y `/v3/api-docs/**`: públicos para facilitar la documentación.

Ejemplo para crear un trabajador (envía el token de administrador en `Authorization: Bearer ...`):

`POST http://localhost:8080/api/v1/usuarios`

```json
{
  "nombre": "Nuevo trabajador",
  "email": "nuevo@mhmstore.com",
  "contrasena": "ClaveSegura123",
  "rol": "TRABAJADOR"
}
```

La contraseña debe tener al menos 12 caracteres y no puede contener espacios al principio ni al final. La API guarda únicamente el hash BCrypt y nunca devuelve la contraseña. Un correo ya registrado devuelve `409 Conflict`; los datos inválidos devuelven `400 Bad Request`.
- `401`: falta autenticación válida o el token no es válido.
- `403`: usuario autenticado sin permisos suficientes.

Esta es una política inicial conservadora: todas las escrituras de la API están restringidas a ADMIN. Ajusta las reglas por ruta si el equipo decide permitir operaciones concretas a TRABAJADOR.

### 4. Pruebas manuales recomendadas en Postman

1. Login con ADMIN y con TRABAJADOR: debe responder `200` y devolver un JWT.
2. `GET http://localhost:8080/api/v1/productos` sin token: debe responder `401`.
3. El mismo GET con token de cualquiera de los roles: debe responder `200`.
4. `DELETE http://localhost:8080/api/v1/productos/1` con token TRABAJADOR: debe responder `403`.
5. El DELETE con token ADMIN y un producto existente: debe responder `204`.
6. Modifica un carácter del token y repite un GET protegido: debe responder `401`.
7. Contraseña incorrecta en login: debe responder `401`.

Ejecuta las pruebas existentes y revisa los tests HTTP: al proteger rutas, los tests anteriores que no envíen autenticación deberán adaptarse con `spring-security-test` o con tokens de prueba. No se ha afirmado que el build esté verificado hasta ejecutar Maven en tu entorno.
