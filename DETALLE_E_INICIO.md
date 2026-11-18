# Detalle de mochilas e inicio

## Sincronización con GitHub
El 18 de noviembre de 2026 se actualizó main desde ebaee31 hasta 8090a1d mediante avance rápido de origin/main (17 commits). Los archivos locales previos coincidían con la versión integrada, comparando el contenido sin diferencias de finales de línea. El respaldo anterior se conserva en Git con el hash e27bba0e60998b05a1032007946ccad34c08b808.
Las mejoras de este documento quedan en el directorio local, pendientes de commit y publicación en GitHub.

## Registrar colores e imágenes
1. En Inventario, crea o edita una mochila y completa su modelo, categoría y nombre de color.
2. Registra cada color con su propio SKU, precio y cantidad. Utiliza el mismo modelo y categoría para que aparezcan juntos en el detalle.
3. Selecciona la muestra de color y sube la imagen principal de ese SKU.
4. Guarda. Al editar un producto ya guardado, puedes añadir hasta seis vistas adicionales desde el bloque de galería. Cada imagen permite previsualizar antes de subir y eliminar con confirmación.
5. Completa material, capacidad, dimensiones y peso con sus unidades. Son campos opcionales; las especificaciones públicas muestran únicamente los valores registrados.

Las variantes se consultan del backend por modelo (sin distinguir mayúsculas) y categoría. Las variantes ocultas no aparecen entre las opciones públicas. Al elegir una, se carga su propio SKU, precio, existencias y galería, y el carrito recibe ese SKU y la cantidad elegida.
No se agregaron productos ni fotografías de ejemplo a MySQL. Los productos antiguos conservan sus datos. Si todavía no tienen fotos o especificaciones, la interfaz muestra su estado correspondiente.

## Inicio
El panel destacado funciona como carrusel con hasta seis productos de la primera página del catálogo, priorizando los que tienen una imagen registrada. Tiene controles anterior/siguiente, indicadores, pausa y avance automático cada 6,5 segundos. Se pausa al pasar el puntero o enfocar controles y no avanza si la pestaña no está visible. Respeta la preferencia del sistema para reducir movimiento. Las secciones se revelan al entrar en pantalla y las tarjetas conservan los enlaces al detalle o a su categoría.

## Datos y endpoints
V5__product_details_and_gallery.sql agrega color_hex, material, capacidad, dimensiones y peso a productos y crea producto_imagenes. La migración ya se aplicó correctamente en el MySQL local durante esta sesión. Los archivos se almacenan en el directorio de imágenes ya existente.

- GET /api/v1/productos/{id}/detalle: producto, variantes y galería.
- GET /api/v1/productos/{id}/galeria: imágenes adicionales.
- POST /api/v1/productos/{id}/galeria: carga de una imagen, solo con permiso de gestión de productos.
- GET /api/v1/productos/{id}/galeria/{imagenId}: imagen asociada al SKU.
- DELETE /api/v1/productos/{id}/galeria/{imagenId}: borrado con autorización.

## Verificación
- npm run build: compilación Angular correcta, sin advertencias de tamaño.
- Maven test: 28 pruebas, 0 fallos, 0 errores. Incluye 5 pruebas nuevas de agrupación de variantes, aislamiento de imágenes por SKU, permisos de carga, rechazo de archivos falsos y persistencia/validación de especificaciones.
- Navegador: inicio y detalle con productos reales, controles del carrusel, cambio de SKU con imagen y existencias propias, menú móvil y vista de 390 x 844.
- La vista de escritorio del navegador se restauró después de la revisión móvil.

## Uso local
MySQL de XAMPP debe estar encendido. Ejecuta el backend con tu configuración Run habitual y Angular con npm start desde frontend. Abre http://localhost:4200/inicio. Los procesos de backend y frontend quedaron iniciados al finalizar la implementación.
