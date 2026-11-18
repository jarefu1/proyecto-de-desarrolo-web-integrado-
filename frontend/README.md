# MHM Store Frontend (Angular) — APF3: Semana 15

Frontend desarrollado en **Angular 22** con arquitectura **Standalone Components**, Signals, Reactive Forms, Guards de autenticación e Interceptors para conectarse con la API REST de **Spring Boot 3 (MHM Store)**.

---

## Requisitos de APF3 Cubiertos

| Requisito | Implementación |
| :--- | :--- |
| **Proyecto Angular Organizado** | Arquitectura por capas: `models/`, `services/`, `guards/`, `interceptors/`, `pages/`, `config/`. |
| **Rutas Principales** | Configuración con lazy loading en `app.routes.ts`: `/inicio`, `/login`, `/productos`, `/carrito`, `/pedidos`, `/reportes`, `/usuarios`. |
| **Formularios con Validación** | Reactive Forms con mensajes de error visibles en pantalla para login, creación/edición de mochilas y alta de usuarios/permisos. |
| **Consumo real de API REST** | Servicios Angular (`ProductosService`, `CarritoPedidoService`, `UsuariosService`, `AuthService`) consumiendo `http://localhost:8080/api/v1`. |
| **CRUD visible desde frontend** | Crear, listar, editar y eliminar mochilas; control de stock; gestión de trabajadores y permisos. |
| **Manejo de errores HTTP** | Captura y traducción de respuestas `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` y `409 Conflict`. |
| **Autenticación desde Angular** | Login con JWT guardado en `localStorage`, envío automático del token en cabecera `Authorization: Bearer ...` mediante `authInterceptor`, y protección de rutas con `authGuard` y `adminGuard`. Las cuentas las crea el administrador. |
| **Evidencia End-to-End** | Flujo completo: Catálogo -> Carrito -> Enviar a caja -> Confirmar pedido transaccional (descuenta stock en MySQL) -> Generar reporte de ventas. |

---

## Cómo Ejecutar el Frontend

1. Asegúrate de tener el Backend de Spring Boot corriendo en `http://localhost:8080`.
2. Abre una terminal en la carpeta `frontend`:
   ```bash
   cd frontend
   npm install   # (solo si lo descomprimes desde el ZIP)
   npm start     # Inicia en http://localhost:4200
   ```
3. Accede a tu navegador en:  
   👉 **http://localhost:4200**

Para apuntar a otro backend, define `globalThis.MHM_API_BASE` antes de cargar el bundle Angular (por ejemplo, desde un `config.js` del despliegue).

---

## Cuentas

No se publican credenciales de demostración. Configura el administrador inicial con `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` en el backend; crea las demás cuentas desde el módulo de administración.
