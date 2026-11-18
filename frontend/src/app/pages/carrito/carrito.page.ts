import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h2>🛒 Carrito de Compras</h2>
          <p class="subtitle">Revisa tu selección y continúa al pago para completar tu compra.</p>
        </div>
      </div>

      @if (mensajeExito()) {
        <div class="alert alert-success">{{ mensajeExito() }}</div>
      }
      @if (mensajeError()) {
        <div class="alert alert-error">{{ mensajeError() }}</div>
      }

      @if (!carritoService.carrito() || carritoService.carrito()!.items.length === 0) {
        <div class="empty-state">
          <h3>Tu carrito está vacío</h3>
          <p>No tienes mochilas agregadas en este momento.</p>
          <a routerLink="/productos" class="btn btn-primary">Ver Catálogo de Mochilas</a>
        </div>
      } @else {
        <div class="cart-layout">
          <div class="cart-items">
            <table class="table">
              <thead>
                <tr>
                  <th>Mochila</th>
                  <th>Precio Unit.</th>
                  <th>Cantidad</th>
                  <th>Subtotal</th>
                  <th>Acción</th>
                </tr>
              </thead>
              <tbody>
                @for (item of carritoService.carrito()!.items; track item.id) {
                  <tr>
                    <td>
                      <strong>{{ item.productoModelo }}</strong>
                      <span class="sku">({{ item.productoCodigo }})</span>
                    </td>
                    <td>S/ {{ item.precioUnitario | number:'1.2-2' }}</td>
                    <td>{{ item.cantidad }}</td>
                    <td><strong>S/ {{ item.subtotal | number:'1.2-2' }}</strong></td>
                    <td>
                      <button (click)="quitarItem(item.productoId)" class="btn-icon delete" title="Quitar">🗑️</button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>

            <div class="cart-footer-actions">
              <button (click)="vaciar()" class="btn btn-outline-danger">Vaciar Carrito</button>
              <a routerLink="/productos" class="btn btn-secondary">Seguir Comprando</a>
            </div>
          </div>

          <div class="cart-summary">
            <h3>Resumen del Pedido</h3>
            <div class="summary-line">
              <span>Estado del Carrito:</span>
              <span class="badge badge-info">{{ carritoService.carrito()!.estado }}</span>
            </div>
            <div class="summary-line">
              <span>Total de Artículos:</span>
              <span>{{ carritoService.totalItemsCarrito() }}</span>
            </div>
            <div class="summary-line total">
              <span>Total a Pagar:</span>
              <span>S/ {{ carritoService.carrito()!.total | number:'1.2-2' }}</span>
            </div>

            <button
              (click)="confirmarPedido()"
              [disabled]="procesando()"
              class="btn btn-success btn-block"
            >
              {{ procesando() ? 'Creando pedido...' : 'Continuar al pago simulado →' }}
            </button>
            <small class="note">Primero reservaremos el stock; luego podrás completar el pago de demostración desde Mis pedidos.</small>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem 0; }
    .page-header { margin-bottom: 2rem; }
    .subtitle { color: #64748b; font-size: 0.9rem; margin: 0.25rem 0 0; }
    .cart-layout { display: grid; grid-template-columns: 2fr 1fr; gap: 2rem; align-items: start; }
    .table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }
    .table th, .table td { padding: 1rem; text-align: left; border-bottom: 1px solid #e2e8f0; }
    .table th { background: #f8fafc; font-weight: 600; color: #334155; }
    .sku { color: #64748b; font-size: 0.8rem; margin-left: 0.25rem; }
    .cart-footer-actions { margin-top: 1rem; display: flex; gap: 0.75rem; }
    .cart-summary { background: white; padding: 1.5rem; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
    .cart-summary h3 { margin-top: 0; color: #1e293b; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; }
    .summary-line { display: flex; justify-content: space-between; margin-bottom: 0.75rem; font-size: 0.95rem; }
    .summary-line.total { border-top: 2px dashed #cbd5e1; padding-top: 1rem; margin-top: 1rem; font-size: 1.25rem; font-weight: 800; color: #0f172a; }
    .btn-block { width: 100%; margin-top: 1rem; padding: 0.85rem; font-size: 1rem; }
    .note { display: block; color: #64748b; font-size: 0.75rem; margin-top: 0.75rem; text-align: center; }
    .empty-state { text-align: center; padding: 4rem 1rem; background: white; border-radius: 12px; border: 1px dashed #cbd5e1; }
    .btn { padding: 0.6rem 1rem; border-radius: 8px; border: none; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-block; }
    .btn-primary { background: #2563eb; color: white; }
    .btn-secondary { background: #64748b; color: white; }
    .btn-success { background: #16a34a; color: white; }
    .btn-outline-danger { background: white; border: 1px solid #ef4444; color: #ef4444; }
    .btn-icon.delete { background: #fee2e2; border: 1px solid #fca5a5; border-radius: 6px; padding: 0.35rem 0.5rem; cursor: pointer; }
    .badge { padding: 0.25rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }
    .badge-info { background: #e0f2fe; color: #0369a1; }
    .alert { padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.5rem; }
    .alert-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
    .alert-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
  `]
})
export class CarritoPage implements OnInit {
  protected readonly carritoService = inject(CarritoPedidoService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly procesando = signal(false);
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');

  ngOnInit(): void {
    this.carritoService.cargarCarritoUsuario();
  }

  quitarItem(productoId: number): void {
    this.carritoService.quitarItem(productoId).subscribe({
      next: () => this.mensajeExito.set('Artículo retirado del carrito.'),
      error: (err) => this.mensajeError.set(this.auth.mensajeError(err))
    });
  }

  vaciar(): void {
    if (!confirm('¿Deseas vaciar todos los productos del carrito?')) return;
    this.carritoService.vaciarCarrito().subscribe({
      next: () => this.mensajeExito.set('Carrito vaciado.'),
      error: (err) => this.mensajeError.set(this.auth.mensajeError(err))
    });
  }

  confirmarPedido(): void {
    this.procesando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');

    this.carritoService.confirmarPedido().subscribe({
      next: (pedido) => {
        this.procesando.set(false);
        this.router.navigate(['/pedidos'], { queryParams: { pedido: pedido.id } });
      },
      error: (err) => {
        this.procesando.set(false);
        this.mensajeError.set(this.auth.mensajeError(err));
      }
    });
  }
}
