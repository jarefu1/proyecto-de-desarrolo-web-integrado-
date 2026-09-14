# Matriz rápida de métodos HTTP


| Método   | URI                      | Entrada                                  | Respuesta            | HTTP esperado    |
| -------- | ------------------------ | ---------------------------------------- | -------------------- | ---------------- |
| `GET`    | `/api/v1/productos`      | Parámetros opcionales de búsqueda/filtro | Lista de productos   | `200 OK`         |
| `GET`    | `/api/v1/productos/{id}` | ID del producto                          | Producto solicitado  | `200 OK`         |
| `POST`   | `/api/v1/productos`      | JSON con datos del producto              | Producto creado      | `201 CREATED`    |
| `PUT`    | `/api/v1/productos/{id}` | ID + JSON con datos del producto         | Producto actualizado | `200 OK`         |
| `PATCH`  | `/api/v1/productos/{id}` | ID + JSON con campos a modificar         | Producto actualizado | `200 OK`         |
| `DELETE` | `/api/v1/productos/{id}` | ID del producto                          | Sin contenido        | `204 NO CONTENT` |
