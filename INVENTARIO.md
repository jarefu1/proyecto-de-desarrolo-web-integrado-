# Catálogo público e inventario

## Pantallas
- `/productos`: catálogo público con búsqueda, categoría, disponibilidad, favoritos y carrito.
- `/inventario`: gestión interna. Requiere ADMIN o permisos de productos, inventario o catálogos.
- `/producto/:id`: detalle. Los productos ocultos solo pueden ser consultados por gestores autorizados.

El CRUD reutiliza ProductosService y los endpoints de productos existentes. Las acciones se habilitan según su permiso específico: GESTIONAR_PRODUCTOS para alta/edición/borrado e imágenes, GESTIONAR_INVENTARIO para entradas/salidas, GESTIONAR_CATALOGOS para categorías. Las restricciones también se aplican en el backend.

## Datos y migración
Flyway ejecuta V4__inventory_metadata.sql al iniciar. Agrega stock_minimo (5 por defecto), activo (true), fecha_creacion y fecha_actualizacion sin eliminar registros. Para productos existentes, las fechas iniciales corresponden al momento de la migración, ya que antes no se registraban.

Stock suficiente: cantidad > stock mínimo. Stock bajo: 0 < cantidad <= stock mínimo. Agotado: cantidad = 0. Los cinco indicadores cuentan todo el inventario, independientemente de los filtros y de la publicación. Ocultar un producto lo retira del catálogo público e impide añadirlo o confirmar su compra. Los pedidos históricos conservan su consulta.

Las imágenes continúan en uploads/productos; MySQL almacena la referencia. Debe respaldarse esa carpeta junto a la base. Los reemplazos y borrados de archivos antiguos se ejecutan después de confirmar la transacción.

## API de consulta administrativa
- GET /api/v1/inventario/productos: página con q, categoria, disponible, stockBajo, page y size.
- GET /api/v1/inventario/resumen: totales globales.
- GET /api/v1/inventario/categorias: categorías y cantidad real de productos asociados.

La eliminación de una categoría con productos devuelve 409. Reasigna sus productos primero.

## Ejecución local con XAMPP
1. Iniciar MySQL en XAMPP.
2. Mantener la configuración local de DB_URL, DB_USERNAME=root, DB_PASSWORD vacío y JWT_SECRET_BASE64 utilizada por el proyecto.
3. Backend desde la raíz: `.\mvnw.cmd spring-boot:run` o el botón Run de la configuración Java existente.
4. Frontend desde frontend: `npm start`.
5. Abrir http://localhost:4200. Entrar con una cuenta autorizada para acceder a Inventario.

## Verificación
Backend: `.\mvnw.cmd test` (H2 aislada, sin tocar MySQL).
Frontend: `npm run build` dentro de frontend.
InventarioCatalogoIntegrationTest cubre visibilidad, permisos, mínimo individual, categorías asociadas, edición y eliminación, y persistencia/lectura de imágenes. Los registros de prueba se revierten con transacciones.

## Archivos de esta implementación
Creados:
- frontend/src/app/pages/inventario/inventario.page.ts
- frontend/src/app/pages/productos/productos.page.html
- frontend/src/app/pages/productos/productos.page.css
- src/main/java/com/mhmstore/api/controller/InventarioConsultaController.java
- src/main/resources/db/migration/V4__inventory_metadata.sql
- src/test/java/com/mhmstore/api/controller/InventarioCatalogoIntegrationTest.java
- INVENTARIO.md

Modificados:
- frontend/src/app/app.ts, app.routes.ts y guards/auth.guard.ts: navegación y autorización.
- frontend/src/app/models/producto.model.ts y services/productos.service.ts: datos y consultas compartidas.
- frontend/src/app/pages/productos/productos.page.ts: catálogo público independiente.
- Backend: model/Producto.java, dto/ProductoRequest.java, dto/ProductoResponse.java, repository/ProductoRepository.java.
- Backend: service/ProductoServiceImpl.java, InventarioServiceImpl.java, CategoriaServiceImpl.java, CarritoServiceImpl.java, PedidoServiceImpl.java.
- Backend: security/SecurityConfig.java.
- Pruebas: InventarioServiceImplTest.java (mock de consulta con bloqueo) y ProductoRepositoryIntegrationTest.java (escaneo JPA explícito).
- .gitignore: excluir las imágenes cargadas del control de versiones.

## Resultado de verificación
- Maven test: 23 pruebas, 0 fallos, 0 errores, 0 omitidas. Incluye 7 pruebas nuevas de integración.
- Angular npm run build: correcto, sin advertencias de presupuesto CSS en la última compilación.
- Angular inició en modo desarrollo en 127.0.0.1:4201; el proceso de revisión fue detenido al terminar.
- La revisión visual y responsive en navegador quedó pendiente: el entorno de automatización no pudo abrir la vista local y agotó el tiempo de conexión.
- La migración MySQL se ejecutará al reiniciar el backend; las pruebas usaron H2 y no modificaron la base MySQL local.
- npm test: no pudo ejecutarse porque angular.json no define un target de pruebas (Cannot determine project or target for command). La compilación de producción sí se completó.
