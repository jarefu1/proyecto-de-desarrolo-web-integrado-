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
