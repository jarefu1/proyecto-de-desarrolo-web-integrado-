import { Component, DestroyRef, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { AuthService } from '../../services/auth.service';
import { MetodoPagoSimulado, Pedido } from '../../models/carrito-pedido.model';
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
        <div class="loading" role="status">Cargando pedidos...</div>
      } @else if (errorPedidos()) {
        <p class="payment-error" role="alert">{{ errorPedidos() }}</p>
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
                @if (ped.metodoPago && ped.fechaPago) {
                  <p class="payment-note">✓ Pago simulado completado · {{ nombreMetodo(ped.metodoPago) }}</p>
                  <button type="button" class="pay-button" (click)="verBoleta(ped)">Ver boleta</button>
                }
                @if (auth.rol() === 'CLIENTE' && ped.estado === 'PENDIENTE' && !ped.fechaPago) {
                  <button type="button" class="pay-button" (click)="abrirPago(ped)">Pagar pedido · S/ {{ ped.total | number:'1.2-2' }} <span>→</span></button>
                }
              </div>
            </div>
          }
        </div>
      }

      <dialog #checkout class="payment-dialog" aria-labelledby="payment-title" (cancel)="cancelarDialogo($event)">
        <button type="button" class="payment-close" [disabled]="pagando()" (click)="checkout.close()" aria-label="Cerrar">×</button>
        @if (boleta(); as pedido) {
          <h3 id="payment-title" tabindex="-1">Boleta simulada B-{{ pedido.id }}</h3>
          @if (pagoAprobado()) {
            <p class="payment-success" role="status">Pago simulado aprobado. Tu boleta está lista.</p>
          }
          <p>MHM Store · Pedido #{{ pedido.id }} · {{ pedido.fechaPago | date:'dd/MM/yyyy HH:mm' }}</p>
          <p>Cliente: {{ pedido.usuarioEmail }}<br>Método: {{ nombreMetodo(pedido.metodoPago ?? '') }}</p>
          <ul class="receipt-items">
            @for (item of pedido.items; track item.id) {
              <li>
                <span>{{ item.cantidad }} × {{ item.productoModelo }} · S/ {{ item.precioUnitario | number:'1.2-2' }} c/u</span>
                <strong>S/ {{ item.subtotal | number:'1.2-2' }}</strong>
              </li>
            }
          </ul>
          <p class="receipt-total">Total pagado: S/ {{ pedido.total | number:'1.2-2' }}</p>
          <p>Comprobante de demostración, sin validez tributaria.</p>
          <button type="button" class="pay-confirm" [disabled]="descargando()" (click)="descargarBoleta(pedido)">
            {{ descargando() ? 'Preparando PDF…' : 'Descargar boleta en PDF' }}
          </button>
          @if (errorDescarga()) {
            <p class="payment-error" role="alert">{{ errorDescarga() }}</p>
          }
        } @else if (pedidoAPagar(); as pedido) {
          <h3 id="payment-title">Pago simulado</h3>
          <p>Pedido #{{ pedido.id }} · <strong>S/ {{ pedido.total | number:'1.2-2' }}</strong></p>
          <p>Esta simulación no realiza cargos ni solicita datos financieros.</p>
          <fieldset class="payment-methods" [disabled]="pagando()">
            <legend>Selecciona un método de pago</legend>
            @for (metodo of metodos; track metodo) {
              <label [class.selected]="metodoPago() === metodo">
                <input type="radio" name="metodoPago" [checked]="metodoPago() === metodo" (change)="metodoPago.set(metodo)">
                {{ nombreMetodo(metodo) }}
              </label>
            }
          </fieldset>
          @if (metodoPago(); as metodo) {
            <div class="simulation-note">
              Confirma el pago de S/ {{ pedido.total | number:'1.2-2' }} con {{ nombreMetodo(metodo) }}.
            </div>
          }
          @if (errorPago()) {
            <p class="payment-error" role="alert">{{ errorPago() }}</p>
          }
          @if (pagando()) {
            <p role="status">Procesando el pago simulado…</p>
          }
          <button type="button" class="pay-confirm" [disabled]="pagando() || !metodoPago()" (click)="confirmarPago()">
            Confirmar pago simulado
          </button>
        }
      </dialog>
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
    .payment-dialog::backdrop { background: rgba(20,25,24,.42); backdrop-filter: blur(7px); }
    .payment-dialog { box-sizing: border-box; width: min(calc(100% - 2rem), 500px); padding: 2rem; border: 0; border-radius: 24px; background: #fafaf9; box-shadow: 0 24px 80px rgba(0,0,0,.22); }
    .payment-close { position: absolute; right: 1rem; top: .8rem; border: 0; background: transparent; color: #737a75; font-size: 1.7rem; cursor: pointer; }
    .payment-dialog h3 { font-size: 1.7rem; letter-spacing: -.06em; margin: .5rem 0; }
    .payment-dialog > p { color: #777d79; font-size: .82rem; line-height: 1.55; }
    .payment-methods { display: grid; gap: .55rem; margin: 1.1rem 0; padding: .55rem 0 0; border: 0; min-width: 0; }
    .payment-methods label { display: flex; gap: .7rem; align-items: center; border: 1px solid var(--line); border-radius: 14px; padding: .85rem; color: #424a45; background: white; cursor: pointer; }
    .payment-methods label.selected { border-color: #252c28; box-shadow: 0 0 0 1px #252c28; }
    .simulation-note { padding: .75rem; margin-bottom: 1rem; border-radius: 12px; background: #eaf0eb; color: #5d6b61; font-size: .71rem; line-height: 1.45; }
    .pay-confirm { width: 100%; }
    button:disabled { opacity: .55; cursor: default; }
    .payment-dialog .payment-success, .payment-error { padding: .75rem; border-radius: 10px; }
    .payment-dialog .payment-success { background: #dcfce7; color: #166534; }
    .payment-dialog .payment-error, .payment-error { background: #fee2e2; color: #991b1b; }
    .receipt-items { list-style: none; padding: 0; }
    .receipt-items li { display: flex; justify-content: space-between; gap: 1rem; padding: .65rem 0; border-bottom: 1px solid #e5e5e5; }
    .receipt-items strong { white-space: nowrap; }
    .payment-dialog .receipt-total { text-align: right; color: #17191b; font-weight: 800; }
  `]
})
export class PedidosPage implements OnInit {
  protected readonly pedidosService = inject(CarritoPedidoService);
  protected readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly checkout = viewChild.required<ElementRef<HTMLDialogElement>>('checkout');
  readonly cargando = signal(true);
  readonly errorPedidos = signal('');
  readonly pedidoAPagar = signal<Pedido | null>(null);
  readonly boleta = signal<Pedido | null>(null);
  readonly pagoAprobado = signal(false);

  imagenCompleta(url: string): string { return `${API_BASE.replace('/api/v1', '')}${url}`; }
  readonly metodos: readonly MetodoPagoSimulado[] = ['TARJETA', 'BILLETERA', 'TRANSFERENCIA'];
  readonly metodoPago = signal<MetodoPagoSimulado | null>(null);
  readonly pagando = signal(false);
  readonly errorPago = signal('');
  readonly descargando = signal(false);
  readonly errorDescarga = signal('');

  nombreMetodo(metodo: string): string {
    return ({ TARJETA: 'Tarjeta de prueba', TRANSFERENCIA: 'Transferencia', BILLETERA: 'Billetera digital' } as Record<string, string>)[metodo] ?? metodo;
  }

  abrirPago(pedido: Pedido): void {
    this.boleta.set(null);
    this.pedidoAPagar.set(pedido);
    this.metodoPago.set(null);
    this.errorPago.set('');
    this.checkout().nativeElement.showModal();
  }

  verBoleta(pedido: Pedido, aprobada = false): void {
    this.pedidoAPagar.set(null);
    this.boleta.set(pedido);
    this.pagoAprobado.set(aprobada);
    this.errorDescarga.set('');
    const dialogo = this.checkout().nativeElement;
    if (!dialogo.open) dialogo.showModal();
    requestAnimationFrame(() => dialogo.querySelector<HTMLElement>('#payment-title')?.focus());
  }

  cancelarDialogo(evento: Event): void {
    if (this.pagando()) evento.preventDefault();
  }

  confirmarPago(): void {
    const pedido = this.pedidoAPagar();
    const metodo = this.metodoPago();
    if (!pedido || !metodo || this.pagando()) return;
    this.errorPago.set('');
    this.pagando.set(true);
    this.pedidosService.pagarSimulado(pedido.id, metodo).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (pedidoPagado) => {
        this.pagando.set(false);
        this.verBoleta(pedidoPagado, true);
      },
      error: (error) => {
        this.pagando.set(false);
        const detalle = error instanceof HttpErrorResponse && error.status === 409
          ? 'El pedido cambió de estado. Actualiza Mis pedidos para consultar el pago y la boleta.'
          : this.auth.mensajeError(error);
        this.errorPago.set(`No se pudo confirmar el pago. ${detalle}`);
      }
    });
  }

  async descargarBoleta(pedido: Pedido): Promise<void> {
    if (this.descargando() || !pedido.fechaPago || !pedido.metodoPago) return;
    this.descargando.set(true);
    this.errorDescarga.set('');
    try {
      const { guardarBoletaPdf } = await import('./boleta-pdf');
      guardarBoletaPdf(pedido, this.nombreMetodo(pedido.metodoPago));
    } catch {
      this.errorDescarga.set('No se pudo preparar el PDF. Inténtalo nuevamente.');
    } finally {
      this.descargando.set(false);
    }
  }

  ngOnInit(): void {
    this.pedidosService.listarPedidos().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (pedidos) => {
        this.cargando.set(false);
        const pedidoId = Number(this.route.snapshot.queryParamMap.get('pedido'));
        const pendiente = pedidos.find(p => p.id === pedidoId && p.estado === 'PENDIENTE' && !p.fechaPago);
        if (pendiente && this.auth.rol() === 'CLIENTE') this.abrirPago(pendiente);
      },
      error: (error) => {
        this.cargando.set(false);
        this.errorPedidos.set(`No se pudieron cargar los pedidos. ${this.auth.mensajeError(error)}`);
      }
    });
  }
}
