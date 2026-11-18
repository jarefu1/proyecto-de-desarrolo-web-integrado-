import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, switchMap, tap, throwError } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { Carrito, CarritoItemRequest, MetodoPagoSimulado, Pedido, ReporteVentas } from '../models/carrito-pedido.model';

@Injectable({ providedIn: 'root' })
export class CarritoPedidoService {
  private readonly http = inject(HttpClient);
  private readonly carritoUrl = `${API_BASE}/carritos`;
  private readonly pedidosUrl = `${API_BASE}/pedidos`;
  private readonly reportesUrl = `${API_BASE}/reportes`;

  private readonly _carrito = signal<Carrito | null>(null);
  private readonly _pedidos = signal<Pedido[]>([]);
  private readonly _reportes = signal<ReporteVentas[]>([]);

  readonly carrito = this._carrito.asReadonly();
  readonly pedidos = this._pedidos.asReadonly();
  readonly reportes = this._reportes.asReadonly();

  readonly totalItemsCarrito = computed(() => {
    const c = this._carrito();
    if (!c || !c.items) return 0;
    return c.items.reduce((acc, item) => acc + item.cantidad, 0);
  });

  cargarCarritoUsuario(): void {
    this.http.get<Carrito>(`${this.carritoUrl}/mi-carrito`).subscribe({
      next: (c) => this._carrito.set(c),
      error: () => console.error('No se pudo cargar el carrito')
    });
  }

  agregarItem(productoId: number, cantidad: number): Observable<Carrito> {
    const c = this._carrito();
    const body: CarritoItemRequest = { productoId, cantidad };
    const carrito$ = c ? of(c)
      : this.http.get<Carrito>(`${this.carritoUrl}/mi-carrito`).pipe(tap((carrito) => this._carrito.set(carrito)));
    return carrito$.pipe(
      switchMap((carrito) => this.http.post<Carrito>(`${this.carritoUrl}/${carrito.id}/items`, body)),
      tap((actualizado) => this._carrito.set(actualizado))
    );
  }

  quitarItem(productoId: number): Observable<Carrito> {
    const carritoId = this._carrito()?.id;
    if (!carritoId) return throwError(() => new Error('El carrito todavía no está cargado.'));
    return this.http.delete<Carrito>(`${this.carritoUrl}/${carritoId}/items/${productoId}`).pipe(
      tap((actualizado) => this._carrito.set(actualizado))
    );
  }

  vaciarCarrito(): Observable<Carrito> {
    const carritoId = this._carrito()?.id;
    if (!carritoId) return throwError(() => new Error('El carrito todavía no está cargado.'));
    return this.http.delete<Carrito>(`${this.carritoUrl}/${carritoId}/items`).pipe(
      tap((actualizado) => this._carrito.set(actualizado))
    );
  }

  confirmarPedido(): Observable<Pedido> {
    const carritoId = this._carrito()?.id;
    if (!carritoId) return throwError(() => new Error('El carrito todavía no está cargado.'));
    return this.http.post<Pedido>(this.pedidosUrl, { carritoId }).pipe(
      tap((pedido) => {
        this._pedidos.update((actual) => [pedido, ...actual]);
        this.cargarCarritoUsuario();
      })
    );
  }

  listarPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.pedidosUrl).pipe(
      tap((peds) => this._pedidos.set(peds))
    );
  }

  pagarSimulado(pedidoId: number, metodo: MetodoPagoSimulado): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.pedidosUrl}/${pedidoId}/pago-simulado`, { metodo }).pipe(
      tap((actualizado) => this._pedidos.update((pedidos) =>
        pedidos.map((pedido) => pedido.id === pedidoId ? actualizado : pedido)))
    );
  }

  generarReporteMensual(mes: number, anio: number): Observable<ReporteVentas> {
    return this.http.post<ReporteVentas>(`${this.reportesUrl}/mensual?mes=${mes}&anio=${anio}`, {}).pipe(
      tap((rep) => this._reportes.update((actual) => [rep, ...actual]))
    );
  }

  listarReportes(): Observable<ReporteVentas[]> {
    return this.http.get<ReporteVentas[]>(this.reportesUrl).pipe(
      tap((reps) => this._reportes.set(reps))
    );
  }
}
