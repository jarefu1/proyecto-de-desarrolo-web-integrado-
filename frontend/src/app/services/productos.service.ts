import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { Almacen, Categoria, Producto, ProductoRequest } from '../models/producto.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ProductosService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly url = `${API_BASE}/productos`;
  private readonly catUrl = `${API_BASE}/categorias`;
  private readonly almUrl = `${API_BASE}/almacenes`;

  private readonly _productos = signal<Producto[]>([]);
  private readonly _categorias = signal<Categoria[]>([]);
  private readonly _almacenes = signal<Almacen[]>([]);
  readonly productos = this._productos.asReadonly();
  readonly categorias = this._categorias.asReadonly();
  readonly almacenes = this._almacenes.asReadonly();

  readonly cargando = signal(false);
  readonly error = signal('');
  readonly total = computed(() => this._productos().length);
  readonly paginaActual = signal(0);
  readonly totalPaginas = signal(0);

  cargarTodo(query = '', categoriaId: number | null = null, inventario = false, disponible: string = ''): void {
    this.cargando.set(true);
    this.error.set('');
    this.cargarPagina(0, false, query, categoriaId, inventario, disponible);

    this.http.get<Categoria[]>(this.catUrl).subscribe({
      next: (cats) => this._categorias.set(cats)
    });

    if (this.auth.estaAutenticado()) {
      this.http.get<Almacen[]>(this.almUrl).subscribe({
        next: (alms) => this._almacenes.set(alms)
      });
    }
  }

  cargarCategorias(): void {
    this.http.get<Categoria[]>(this.catUrl).subscribe({
      next: (categorias) => this._categorias.set(categorias)
    });
  }

  cargarPagina(page: number, stockBajo = false, query = '', categoriaId: number | null = null, inventario = false, disponible: string = ''): void {
    this.cargando.set(true);
    this.error.set('');
    const params = new URLSearchParams({ page: String(page), size: '20' });
    if (disponible) params.set('disponible', disponible);
    if (stockBajo) params.set('stockBajo', 'true');
    if (query.trim()) params.set('q', query.trim());
    if (categoriaId !== null) params.set('categoria', String(categoriaId));
    this.http.get<{ content: Producto[]; number: number; totalPages: number }>(`${inventario ? API_BASE + '/inventario/productos' : this.url + '/pagina'}?${params}`)
      .subscribe({
        next: (resultado) => {
          this._productos.set(resultado.content);
          this.paginaActual.set(resultado.number);
          this.totalPaginas.set(resultado.totalPages);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la lista de productos.');
          this.cargando.set(false);
        }
      });
  }

  resumenInventario(): Observable<ResumenInventario> { return this.http.get<ResumenInventario>(`${API_BASE}/inventario/resumen`); }
  categoriasInventario(): Observable<CategoriaInventario[]> { return this.http.get<CategoriaInventario[]>(`${API_BASE}/inventario/categorias`); }
  guardarCategoria(id: number | null, nombre: string, descripcion = ''): Observable<Categoria> {
    const datos = { nombre, descripcion };
    return (id ? this.http.put<Categoria>(`${this.catUrl}/${id}`, datos) : this.http.post<Categoria>(this.catUrl, datos)).pipe(tap(() => this.cargarCategorias()));
  }
  eliminarCategoria(id: number): Observable<void> { return this.http.delete<void>(`${this.catUrl}/${id}`).pipe(tap(() => this.cargarCategorias())); }

  obtenerPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.url}/${id}`);
  }

  crear(request: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.url, request).pipe(
      tap((creado) => this._productos.update((actual) => [creado, ...actual]))
    );
  }

  guardarImagen(id: number, archivo: File): Observable<Producto> {
    const datos = new FormData();
    datos.append('imagen', archivo);
    return this.http.post<Producto>(`${this.url}/${id}/imagen`, datos).pipe(
      tap((actualizado) => this._productos.update((actuales) => actuales.map((p) => p.id === id ? actualizado : p)))
    );
  }

  actualizar(id: number, request: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.url}/${id}`, request).pipe(
      tap((actualizado) => {
        this._productos.update((actual) =>
          actual.map((p) => (p.id === id ? actualizado : p))
        );
      })
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`).pipe(
      tap(() => {
        this._productos.update((actual) => actual.filter((p) => p.id !== id));
      })
    );
  }

  ajustarStock(id: number, tipo: 'entrada' | 'salida', cantidad: number): Observable<Producto> {
    return this.http.patch<Producto>(`${API_BASE}/inventario/${id}/${tipo}`, { cantidad }).pipe(
      tap((actualizado) => {
        this._productos.update((actual) =>
          actual.map((p) => (p.id === id ? actualizado : p))
        );
      })
    );
  }
}

export interface ResumenInventario { total: number; disponibles: number; bajos: number; agotados: number; categorias: number; }
export interface CategoriaInventario extends Categoria { productos: number; }
