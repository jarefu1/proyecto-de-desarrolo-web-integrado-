# Inventario: lista, tarjetas e imágenes por color

- La vista inicial del inventario es Lista. Los botones Lista y Tarjetas conservan los filtros, la página, los datos y las acciones autorizadas. La preferencia se guarda en este navegador.
- La lista presenta imagen, modelo, ID/SKU, categoría, color, precio, existencias, stock mínimo, disponibilidad, publicación, almacén y acciones. En móvil la tabla se puede desplazar horizontalmente.
- Las acciones de editar, eliminar y ajustar stock reutilizan los métodos del inventario y respetan sus permisos.

## Subir imágenes
En Añadir/Editar mochila, el bloque Imagen principal contiene un selector «¿Qué color representa esta imagen?». Al crear, la opción corresponde al color del nuevo SKU. Al editar, también aparecen las otras variantes registradas con el mismo modelo y categoría, identificadas por color y SKU. Para crear un color nuevo, registra antes su variante con su propio stock y precio.

El selector del bloque de imágenes adicionales permite elegir el color, consultar sus fotos y subir otra con previsualización. La imagen se guarda en ese SKU y aparece en su detalle público. Los colores ocultos se ofrecen únicamente a gestores autorizados.

Las imágenes principales y adicionales aceptan el parámetro opcional varianteId. El backend valida que el destino pertenezca al mismo modelo y categoría, mantiene los permisos de escritura existentes y rechaza destinos ajenos. La API anterior sigue funcionando sin el parámetro.

No se requiere una migración nueva: se reutiliza la asociación persistente de imágenes y colores por SKU.

## Archivos principales
- frontend/src/app/pages/inventario/inventario.page.ts, .html y .css.
- frontend/src/app/components/inventario-lista.component.ts.
- frontend/src/app/components/inventario-vista.component.ts.
- frontend/src/app/components/galeria-editor.component.ts.
- frontend/src/app/services/productos.service.ts.
- Backend: ProductoController, ProductoPresentacionController, ProductoService, ProductoServiceImpl, ProductoPresentacionService y ProductoRepository.
- Pruebas: ProductoDetalleIntegrationTest.

## Verificación
La suite del backend pasó 32 pruebas, sin fallos, errores ni omisiones. Cuatro pruebas nuevas comprueban: guardar la foto principal en la variante elegida, guardar la foto adicional en la variante elegida y rechazar otro modelo/categoría, ocultar variantes privadas a visitantes y permitir su elección a gestores.
La revisión visual del inventario autenticado no pudo completarse: la herramienta de navegador agotó el tiempo de conexión y después no encontró la pestaña existente.

Para usarlo, inicia MySQL de XAMPP, ejecuta el backend con Run y el frontend con npm start; abre /inventario con tu cuenta autorizada. Reinicia el backend si todavía estaba ejecutando la versión anterior.
