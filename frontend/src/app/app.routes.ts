import { Routes } from '@angular/router';
import { authGuard, adminGuard, inventarioGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'inicio'
  },
  {
    path: 'inicio',
    title: 'Inicio | MHM Store',
    loadComponent: () => import('./pages/inicio/inicio.page').then((m) => m.InicioPage)
  },
  {
    path: 'login',
    title: 'Iniciar Sesión | MHM Store',
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage)
  },
  {
    path: 'productos',
    title: 'Catálogo de Mochilas | MHM Store',
    loadComponent: () => import('./pages/productos/productos.page').then((m) => m.ProductosPage)
  },
  {
    path: 'producto/:id',
    title: 'Detalle del producto | MHM Store',
    loadComponent: () => import('./pages/producto-detalle/producto-detalle.page').then((m) => m.ProductoDetallePage)
  },
  {
    path: 'favoritos',
    title: 'Favoritos | MHM Store',
    loadComponent: () => import('./pages/favoritos/favoritos.page').then((m) => m.FavoritosPage)
  },
  {
    path: 'perfil',
    title: 'Mi cuenta | MHM Store',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/perfil/perfil.page').then((m) => m.PerfilPage)
  },
  { path: 'inventario', title: 'Inventario | MHM Store', canActivate: [inventarioGuard], loadComponent: () => import('./pages/inventario/inventario.page').then(m => m.InventarioPage) },
  {
    path: 'carrito',
    title: 'Carrito de Compras | MHM Store',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/carrito/carrito.page').then((m) => m.CarritoPage)
  },
  {
    path: 'pedidos',
    title: 'Historial de Pedidos | MHM Store',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/pedidos/pedidos.page').then((m) => m.PedidosPage)
  },
  {
    path: 'reportes',
    title: 'Reportes de Ventas | MHM Store',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/reportes/reportes.page').then((m) => m.ReportesPage)
  },
  {
    path: 'usuarios',
    title: 'Administración de Usuarios | MHM Store',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/usuarios/usuarios.page').then((m) => m.UsuariosPage)
  },
  {
    path: '**',
    title: 'Página no encontrada | MHM Store',
    loadComponent: () => import('./pages/no-encontrado/no-encontrado.page').then((m) => m.NoEncontradoPage)
  }
];
