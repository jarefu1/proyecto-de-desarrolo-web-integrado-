export type Rol = 'ADMIN' | 'TRABAJADOR' | 'CLIENTE';

export type Permiso =
  | 'VER_CATALOGOS'
  | 'GESTIONAR_PRODUCTOS'
  | 'GESTIONAR_INVENTARIO'
  | 'GESTIONAR_CATALOGOS'
  | 'GESTIONAR_CARRITO'
  | 'GESTIONAR_PEDIDOS'
  | 'VER_REPORTES';

export interface LoginRequest {
  email: string;
  contrasena: string;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  contrasena: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  email: string;
  rol: Rol;
  permisos: Permiso[];
}

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  fechaRegistro: string;
  permisos: Permiso[];
}

export interface UsuarioCreateRequest {
  nombre: string;
  email: string;
  contrasena: string;
  rol: Rol;
  permisos?: Permiso[];
}

export interface UsuarioUpdateRequest {
  nombre?: string;
  email?: string;
  contrasena?: string;
  rol?: Rol;
  permisos?: Permiso[];
}
