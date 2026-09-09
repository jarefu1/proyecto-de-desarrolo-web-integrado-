# proyecto-de-desarrolo-web-integrado-
# MHM STORE API

API REST desarrollada con **Spring Boot** para la gestión del inventario de la tienda de mochilas **MHM STORE**.

El sistema permite registrar, consultar, modificar y eliminar productos, además de consultar su disponibilidad y almacén. En esta primera etapa, la información se almacena temporalmente **en memoria**, sin utilizar una base de datos.

## 1. Problema y contexto

La tienda de mochilas **MHM STORE** presenta dificultades en la consulta y gestión de su inventario durante el proceso de atención al cliente. Los productos se encuentran distribuidos en dos almacenes, por lo que los trabajadores deben realizar consultas manuales para verificar la disponibilidad de los diferentes modelos, colores y cantidades.

Esta situación puede generar demoras en la atención, debido a que la información de los productos no se encuentra centralizada.

La API busca solucionar este problema proporcionando una forma centralizada de consultar y administrar la información del inventario mediante una API REST.

Los principales usuarios son los trabajadores de la tienda. Los clientes se benefician indirectamente al recibir una atención más rápida y contar con información precisa sobre la disponibilidad de los productos.

## 2. Alcance

### Funcionalidades incluidas

* Registrar productos.
* Listar productos.
* Consultar un producto por su identificador.
* Modificar completamente un producto.
* Modificar parcialmente un producto.
* Eliminar productos.
* Consultar disponibilidad.
* Filtrar productos por almacén.
* Filtrar productos por disponibilidad.
* Consultar productos con stock bajo.
* Buscar productos.
* Validar los datos enviados.
* Controlar productos inexistentes.
* Controlar códigos de producto duplicados.

### Funcionalidades excluidas

En esta primera etapa no se incluye:

* Base de datos real.
* JPA/Hibernate.
* Autenticación mediante JWT.
* Gestión de usuarios y roles.
* Procesamiento de pagos.
* Gestión de ventas.
* Despliegue en servicios cloud.
* Interfaz web.

## 3. Tecnologías utilizadas

* **Java 17**
* **Spring Boot**
* **Spring Web MVC**
* **Spring Validation**
* **Maven**
* **JUnit**
* **MockMvc**
* **Mockito**
* Git / GitHub

## 4. Requisitos previos

Para ejecutar el proyecto se necesita tener instalado:

* Java JDK 17 o superior.
* Git, si se desea clonar el repositorio.
* Maven no es obligatorio, ya que el proyecto incluye Maven Wrapper.

Verificar Java:

```bash
java -version
```

La versión utilizada en el proyecto es Java 17.

## 5. Clonar el proyecto

Clonar el repositorio:

```bash
git clone <URL_DEL_REPOSITORIO>
```

Ingresar a la carpeta del proyecto:

```bash
cd mhm-store-api
```

## 6. Ejecutar el proyecto

### Windows

Ejecutar:

```bash
mvnw.cmd spring-boot:run
```

### Linux / macOS

Ejecutar:

```bash
./mvnw spring-boot:run
```

Una vez iniciada la aplicación, la API estará disponible en:

```text
http://localhost:8080
```

El recurso principal de productos se encuentra en:

```text
http://localhost:8080/api/v1/productos
```

## 7. Ejecutar las pruebas

Para ejecutar todos los tests:

### Windows

```bash
mvnw.cmd test
```

### Linux / macOS

```bash
./mvnw test
```

Las pruebas incluyen:

* Pruebas de la capa Service.
* Pruebas de contrato HTTP mediante MockMvc.

## 8. Endpoints principales

| Método | URI                      | Descripción             | HTTP |
| ------ | ------------------------ | ----------------------- | ---- |
| GET    | `/api/v1/productos`      | Listar productos        | 200  |
| GET    | `/api/v1/productos/{id}` | Obtener producto por ID | 200  |
| POST   | `/api/v1/productos`      | Crear producto          | 201  |
| PUT    | `/api/v1/productos/{id}` | Reemplazar producto     | 200  |
| PATCH  | `/api/v1/productos/{id}` | Actualizar parcialmente | 200  |
| DELETE | `/api/v1/productos/{id}` | Eliminar producto       | 204  |

### Errores principales

| Situación                    | HTTP            |
| ---------------------------- | --------------- |
| Datos de entrada inválidos   | 400 Bad Request |
| Producto no encontrado       | 404 Not Found   |
| Código de producto duplicado | 409 Conflict    |

## 9. Ejemplo: crear producto

### Request

```http
POST /api/v1/productos
Content-Type: application/json
```

Body:

```json
{
  "codigo": "MHM001",
  "modelo": "Mochila Urbana",
  "color": "Negro",
  "categoria": "URBANA",
  "precio": 89.90,
  "cantidad": 15,
  "almacen": "PRINCIPAL"
}
```

### Respuesta esperada

```http
201 Created
```

Ejemplo:

```json
{
  "id": 1,
  "codigo": "MHM001",
  "modelo": "Mochila Urbana",
  "color": "Negro",
  "categoria": "URBANA",
  "precio": 89.90,
  "cantidad": 15,
  "almacen": "PRINCIPAL"
}
```

## 10. Ejemplo: listar productos

```http
GET /api/v1/productos
```

Respuesta:

```http
200 OK
```

También se pueden utilizar filtros mediante parámetros:

```http
GET /api/v1/productos?almacen=PRINCIPAL
```

```http
GET /api/v1/productos?disponible=true
```

```http
GET /api/v1/productos?stockBajo=true
```

```http
GET /api/v1/productos?q=urbana
```

## 11. Ejemplo: obtener producto

```http
GET /api/v1/productos/1
```

Si el producto existe:

```http
200 OK
```

Si no existe:

```http
404 Not Found
```

## 12. Ejemplo: actualizar producto

```http
PUT /api/v1/productos/1
Content-Type: application/json
```

Body:

```json
{
  "codigo": "MHM001",
  "modelo": "Mochila Urbana Premium",
  "color": "Negro",
  "categoria": "URBANA",
  "precio": 109.90,
  "cantidad": 20,
  "almacen": "PRINCIPAL"
}
```

Respuesta:

```http
200 OK
```

## 13. Ejemplo: actualización parcial

```http
PATCH /api/v1/productos/1
Content-Type: application/json
```

Por ejemplo, para modificar solamente la cantidad:

```json
{
  "cantidad": 25
}
```

Respuesta:

```http
200 OK
```

## 14. Ejemplo: eliminar producto

```http
DELETE /api/v1/productos/1
```

Respuesta:

```http
204 No Content
```

## 15. Arquitectura del proyecto

El proyecto utiliza una arquitectura por capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Almacenamiento en memoria
```

### Controller

Se encarga de recibir las solicitudes HTTP y devolver las respuestas correspondientes.

```text
controller/
├── ProductoController.java
└── GlobalExceptionHandler.java
```

### Service

Contiene la lógica de negocio relacionada con los productos.

```text
service/
├── ProductoService.java
└── ProductoServiceImpl.java
```

### Repository

Define las operaciones de acceso a los datos y proporciona una implementación en memoria.

```text
repository/
├── ProductoRepository.java
└── InMemoryProductoRepository.java
```

### Model

Contiene las clases principales del dominio.

```text
model/
├── Producto.java
├── Almacen.java
└── Categoria.java
```

### DTO

Contiene los objetos utilizados para recibir y devolver información mediante la API.

```text
dto/
├── ProductoRequest.java
├── ProductoPatchRequest.java
├── ProductoResponse.java
└── ApiErrorResponse.java
```

### Exceptions

Contiene las excepciones utilizadas para controlar errores de negocio.

```text
exception/
├── ProductoNotFoundException.java
└── CodigoDuplicadoException.java
```

## 16. Estructura general

```text
mhm-store-api/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── mhmstore/
│   │   │           └── api/
│   │   │               ├── controller/
│   │   │               ├── dto/
│   │   │               ├── exception/
│   │   │               ├── model/
│   │   │               ├── repository/
│   │   │               └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── mhmstore/
│                   └── api/
│
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## 17. Manejo de errores

La aplicación cuenta con un manejador global de excepciones mediante `GlobalExceptionHandler`.

Los principales errores manejados son:

### 400 Bad Request

Se devuelve cuando los datos enviados no cumplen las validaciones establecidas.

Ejemplos:

* Código vacío.
* Modelo vacío.
* Color vacío.
* Precio menor o igual a cero.
* Cantidad negativa.
* Categoría no especificada.
* Almacén no especificado.

### 404 Not Found

Se devuelve cuando se solicita un producto que no existe.

### 409 Conflict

Se devuelve cuando se intenta registrar un producto utilizando un código que ya existe.

## 18. Almacenamiento

En esta primera versión los productos se almacenan utilizando:

```text
InMemoryProductoRepository
```

Por lo tanto, la información se mantiene únicamente mientras la aplicación está ejecutándose.

Al reiniciar el servidor, los datos almacenados en memoria se pierden.

No se utiliza una base de datos ni JPA en esta etapa.

## 19. Pruebas y TDD

El proyecto incorpora pruebas automatizadas para validar:

* La lógica de negocio del Service.
* El comportamiento HTTP del Controller mediante MockMvc.
* Casos exitosos.
* Casos de error.

También se utiliza un caso de desarrollo basado en:

```text
RED → GREEN → REFACTOR
```

Ejemplo: consulta de un producto inexistente.

```text
RED
↓
La prueba falla porque todavía no se maneja
correctamente el producto inexistente.

GREEN
↓
Se implementa ProductoNotFoundException
y la prueba pasa.

REFACTOR
↓
Se centraliza la búsqueda del producto en
un método reutilizable del Service.
```

## 20. Estado del proyecto

Esta versión corresponde a la primera etapa del proyecto MHM STORE.

Actualmente incluye:

* API REST.
* CRUD de productos.
* Almacenamiento en memoria.
* Validaciones.
* Manejo de errores.
* Arquitectura Controller → Service → Repository.
* Pruebas automatizadas.
* Preparación para pruebas mediante Postman.

Las funcionalidades de persistencia mediante base de datos, autenticación, usuarios, ventas y frontend quedan fuera de esta primera etapa.
