# Pruebas con curl

```bash
# Listar todos los productos
curl -i http://localhost:8080/api/v1/productos

# Filtrar por almacén
curl -i "http://localhost:8080/api/v1/productos?almacen=PRINCIPAL"

# Filtrar productos disponibles
curl -i "http://localhost:8080/api/v1/productos?disponible=true"

# Filtrar productos con stock bajo
curl -i "http://localhost:8080/api/v1/productos?stockBajo=true"

# Buscar productos
curl -i "http://localhost:8080/api/v1/productos?q=mochila"

# Consultar un producto por ID
curl -i http://localhost:8080/api/v1/productos/1

# Crear un producto
curl -i -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -d '{
    "codigo":"MHM001",
    "modelo":"Mochila Urbana",
    "color":"Negro",
    "categoria":"URBANA",
    "precio":89.90,
    "cantidad":20,
    "almacen":"PRINCIPAL"
  }'

# Reemplazar completamente un producto
curl -i -X PUT http://localhost:8080/api/v1/productos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "codigo":"MHM001",
    "modelo":"Mochila Urbana Premium",
    "color":"Azul",
    "categoria":"URBANA",
    "precio":109.90,
    "cantidad":15,
    "almacen":"PRINCIPAL"
  }'

# Actualización parcial
curl -i -X PATCH http://localhost:8080/api/v1/productos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "cantidad":10
  }'

# Actualización parcial del precio
curl -i -X PATCH http://localhost:8080/api/v1/productos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "precio":99.90
  }'

# Eliminar un producto
curl -i -X DELETE http://localhost:8080/api/v1/productos/1
```
