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
  colorHex?: string | null;
  material?: string | null;
  capacidad?: string | null;
  dimensiones?: string | null;
  peso?: string | null;
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
  colorHex?: string | null;
  material?: string | null;
  capacidad?: string | null;
  dimensiones?: string | null;
  peso?: string | null;
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

export interface FotoProducto { id: number; url: string; }
export interface DetalleProducto { producto: Producto; variantes: Producto[]; imagenes: FotoProducto[]; }
