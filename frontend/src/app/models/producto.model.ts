export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
}

export interface Almacen {
  id: number;
  nombre: string;
  ubicacion: string;
  descripcion?: string;
}

export interface Producto {
  id: number;
  codigo: string;
  modelo: string;
  color: string;
  stockMinimo: number;
  activo: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  descripcion?: string | null;
  categoriaId: number;
  categoriaNombre: string;
  precio: number;
  cantidad: number;
  almacenId: number;
  almacenNombre: string;
  imagenUrl: string | null;
}

export interface ProductoRequest {
  codigo: string;
  modelo: string;
  color: string;
  stockMinimo: number;
  activo: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  descripcion?: string | null;
  categoriaId: number;
  precio: number;
  cantidad: number;
  almacenId: number;
}
