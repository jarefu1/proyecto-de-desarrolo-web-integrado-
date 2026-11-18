import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { AuthService } from '../../services/auth.service';
import { MetodoPagoSimulado } from '../../models/carrito-pedido.model';
import { ActivatedRoute } from '@angular/router';
import { API_BASE } from '../../config/api.config';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <div>
          <span class="eyebrow">MHM STORE · TU CUENTA</span>
          <h2>{{ auth.rol() === 'CLIENTE' ? 'Mis pedidos' : 'Pedidos' }}</h2>
          <p class="subtitle">Revisa el estado y el detalle de tus compras.</p>
        </div>
      </header>

      @if (cargando()) {
        <div class="loading">Cargando pedidos...</div>
      } @else if (pedidosService.pedidos().length === 0) {
        <div class="empty-state">No se han registrado pedidos todavía.</div>
      } @else {
        <div class="pedidos-list">
          @for (ped of pedidosService.pedidos(); track ped.id) {
            <div class="pedido-card">
              <div class="pedido-header">
                <div>
                  <h3>Pedido #{{ ped.id }}</h3>
                  <span class="pedido-date">Fecha: {{ ped.fechaEnvio | date:'dd/MM/yyyy HH:mm' }}</span>
                </div>
                <div class="header-right">
                  <span class="badge" [class.badge-success]="ped.estado === 'COMPLETADO'" [class.badge-warning]="ped.estado !== 'COMPLETADO'">
                    {{ ped.estado }}
                  </span>
                  <div class="pedido-total">S/ {{ ped.total | number:'1.2-2' }}</div>
                </div>
              </div>

              <div class="pedido-body">
                @if (auth.rol() !== 'CLIENTE') { <p class="user-info">Cliente: <strong>{{ ped.usuarioEmail }}</strong></p> }
                <div class="items-list">
                  <h4>Artículos incluidos:</h4>
                  <ul>
                    @for (item of ped.items; track item.id) {
                      <li>
                        @if (item.productoImagenUrl) { <img class="order-thumb" [src]="imagenCompleta(item.productoImagenUrl)" [alt]="item.productoModelo" /> }
                        @else { <span class="order-thumb placeholder">▧</span> }
                        {{ item.cantidad }}x {{ item.productoModelo }} ({{ item.productoCodigo }}) —
                        S/ {{ item.subtotal | number:'1.2-2' }}
                      </li>
                    }
                  </ul>
                </div>
                @if (ped.estado === 'COMPLETADO' && ped.metodoPago) {
                  <p class="payment-note">✓ Pago simulado completado · {{ nombreMetodo(ped.metodoPago) }}</p>
                }
                @if (auth.rol() === 'CLIENTE' && ped.estado === 'PENDIENTE') {
                  <button class="pay-button" (click)="pedidoAPagar.set(ped.id)">Pagar pedido · S/ {{ ped.total | number:'1.2-2' }} <span>→</span></button>
                }
              </div>
            </div>
          }
        </div>
      }

      @if (pedidoAPagar()) {
        <div class="payment-backdrop" (click)="pedidoAPagar.set(null)">
          <section class="payment-dialog" role="dialog" aria-modal="true" aria-labelledby="payment-title" (click)="$event.stopPropagation()">
            <button class="payment-close" (click)="pedidoAPagar.set(null)" aria-label="Cerrar">×</button>
            <span class="eyebrow">CHECKOUT SEGURO · DEMOSTRACIÓN</span>
            <h3 id="payment-title">Completa tu compra</h3>
            <p>Selecciona un método. Esta simulación no cobra ni envía datos financieros.</p>
            <div class="payment-methods">
              <button [class.selected]="metodoPago() === 'TARJETA'" (click)="metodoPago.set('TARJETA')">▣ <span>Tarjeta de prueba</span></button>
              <button [class.selected]="metodoPago() === 'BILLETERA'" (click)="metodoPago.set('BILLETERA')">◉ <span>Billetera digital</span></button>
              <button [class.selected]="metodoPago() === 'TRANSFERENCIA'" (click)="metodoPago.set('TRANSFERENCIA')">⇄ <span>Transferencia</span></button>
            </div>
            <div class="simulation-note">La tienda marcará el pedido como pagado para completar el flujo de demostración.</div>
            <button class="pay-confirm" [disabled]="pagando()" (click)="confirmarPago()">{{ pagando() ? 'Procesando...' : 'Confirmar pago simulado' }}</button>
          </section>
        </div>
      }
      @if (mensajePago()) { <div class="payment-toast">{{ mensajePago() }}</div> }
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem 0; }
    .page-header { margin-bottom: 2rem; }
    .subtitle { color: #64748b; font-size: 0.9rem; margin: 0.25rem 0 0; }
    .pedidos-list { display: flex; flex-direction: column; gap: 1.25rem; }
    .pedido-card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
    .pedido-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; margin-bottom: 0.75rem; }
    .pedido-header h3 { margin: 0; color: #1e293b; }
    .pedido-date { color: #64748b; font-size: 0.85rem; }
    .header-right { text-align: right; }
    .pedido-total { font-size: 1.25rem; font-weight: 800; color: #16a34a; margin-top: 0.25rem; }
    .user-info { margin: 0 0 0.5rem; color: #475569; font-size: 0.9rem; }
    .items-list h4 { margin: 0.5rem 0 0.25rem; font-size: 0.9rem; color: #334155; }
    .items-list ul { margin: 0; padding-left: 1.25rem; color: #64748b; font-size: 0.875rem; }
    .badge { padding: 0.25rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }
    .badge-success { background: #dcfce7; color: #15803d; }
    .badge-warning { background: #fef3c7; color: #b45309; }
    .empty-state { text-align: center; padding: 3rem; background: white; border-radius: 8px; color: #64748b; }
    .page-container { padding: .5rem 0 1rem; }
    .eyebrow { color: #828783; font-size: .62rem; letter-spacing: .15em; font-weight: 800; }
    .page-header h2 { margin: .35rem 0 0; font-size: clamp(1.8rem, 3vw, 2.5rem); letter-spacing: -.065em; color: var(--ink); }
    .subtitle { color: #777d79; }
    .pedido-card { background: rgba(255,255,255,.76); border: 1px solid rgba(255,255,255,.86); border-radius: 18px; box-shadow: 0 8px 28px rgba(40,47,44,.055); }
    .pedido-header { border-color: var(--line); }
    .pedido-header h3 { color: var(--ink); }
    .pedido-date, .user-info, .items-list ul { color: #777d79; }
    .pedido-total { color: var(--ink); }
    .payment-note { color: #52705f; font-size: .78rem; font-weight: 700; }
    .pedido-card { background:#fff; border:1px solid #e5e5e5; box-shadow:none; }
    .pedido-header { border-color:#e8e8e8; }
    .order-thumb { width:48px;height:48px;object-fit:contain;display:inline-grid;place-items:center;vertical-align:middle;margin-right:.6rem;border-radius:9px;background:#f1f1f1;color:#999; }
    .pay-button, .pay-confirm { border: 0; border-radius: 999px; padding: .8rem 1.1rem; color: white; background: #17191b; font-weight: 700; cursor: pointer; }
    .pay-button { display: flex; align-items: center; justify-content: space-between; gap: 2rem; margin-top: 1rem; }
    .pay-button:hover, .pay-confirm:hover { background: #bc6654; }
    .payment-backdrop { position: fixed; inset: 0; z-index: 1000; background: rgba(20,25,24,.42); backdrop-filter: blur(7px); display: grid; place-items: center; padding: 1rem; }
    .payment-dialog { position: relative; width: min(100%, 470px); padding: 2rem; border-radius: 24px; background: rgba(250,250,249,.97); box-shadow: 0 24px 80px rgba(0,0,0,.22); }
    .payment-close { position: absolute; right: 1rem; top: .8rem; border: 0; background: transparent; color: #737a75; font-size: 1.7rem; cursor: pointer; }
    .payment-dialog h3 { font-size: 1.7rem; letter-spacing: -.06em; margin: .5rem 0; }
    .payment-dialog > p { color: #777d79; font-size: .82rem; line-height: 1.55; }
    .payment-methods { display: grid; gap: .55rem; margin: 1.1rem 0; }
    .payment-methods button { display: flex; gap: .7rem; align-items: center; border: 1px solid var(--line); border-radius: 14px; padding: .85rem; text-align: left; color: #424a45; background: white; cursor: pointer; }
    .payment-methods button.selected { border-color: #252c28; box-shadow: 0 0 0 1px #252c28; }
    .simulation-note { padding: .75rem; margin-bottom: 1rem; border-radius: 12px; background: #eaf0eb; color: #5d6b61; font-size: .71rem; line-height: 1.45; }
    .pay-confirm { width: 100%; }
    .pay-confirm:disabled { opacity: .55; cursor: wait; }
    .payment-toast { position: fixed; right: 24px; bottom: 24px; z-index: 1100; padding: .9rem 1.2rem; background: #e4eee5; color: #41614a; border-radius: 14px; box-shadow: var(--shadow); }
  `]
})
export class PedidosPage implements OnInit {
  protected readonly pedidosService = inject(CarritoPedidoService);
  protected readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  readonly cargando = signal(false);
  readonly pedidoAPagar = signal<number | null>(null);

  imagenCompleta(url: string): string { return `${API_BASE.replace('/api/v1', '')}${url}`; }
  readonly metodoPago = signal<MetodoPagoSimulado>('TARJETA');
  readonly pagando = signal(false);
  readonly mensajePago = signal('');

  nombreMetodo(metodo: string): string {
    return ({ TARJETA: 'Tarjeta de prueba', TRANSFERENCIA: 'Transferencia', BILLETERA: 'Billetera digital' } as Record<string, string>)[metodo] ?? metodo;
  }

  confirmarPago(): void {
    const id = this.pedidoAPagar();
    if (!id || this.pagando()) return;
    this.pagando.set(true);
    this.pedidosService.pagarSimulado(id, this.metodoPago()).subscribe({
      next: () => {
        this.pagando.set(false);
        this.pedidoAPagar.set(null);
        this.mensajePago.set('Pago simulado aprobado. ¡Gracias por tu compra!');
        setTimeout(() => this.mensajePago.set(''), 4000);
      },
      error: () => this.pagando.set(false)
    });
  }

  ngOnInit(): void {
    this.cargando.set(true);
    this.pedidosService.listarPedidos().subscribe({
      next: () => {
        this.cargando.set(false);
        const pedidoId = Number(this.route.snapshot.queryParamMap.get('pedido'));
        if (pedidoId && this.auth.rol() === 'CLIENTE' && this.pedidosService.pedidos().some(p => p.id === pedidoId && p.estado === 'PENDIENTE')) {
          this.pedidoAPagar.set(pedidoId);
        }
      },
      error: () => this.cargando.set(false)
    });
  }
}
