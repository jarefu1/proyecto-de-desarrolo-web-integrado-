import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductosService } from '../../services/productos.service';
import { AuthService } from '../../services/auth.service';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { Producto } from '../../models/producto.model';
import { API_BASE } from '../../config/api.config';

@Component({ selector: 'app-producto-detalle', standalone: true, imports: [CommonModule, RouterLink], template: `
  <div class="detail-page">
    <nav class="breadcrumb"><a routerLink="/inicio">Inicio</a><span>›</span><a routerLink="/productos">Catálogo</a><span>›</span><span>{{ producto()?.modelo || 'Producto' }}</span></nav>
    @if (cargando()) { <p class="state">Cargando producto…</p> }
    @else if (!producto()) { <div class="state"><h2>No encontramos este producto</h2><a routerLink="/productos">Volver al catálogo</a></div> }
    @else {
      <section class="product-detail"><div class="visual">
        @if (producto()!.imagenUrl) { <img [src]="imagenCompleta(producto()!.imagenUrl!)" [alt]="producto()!.modelo" /> }
        @else { <div class="placeholder"><b>▧</b><span>Imagen no disponible</span></div> }
      </div><div class="info"><span class="eyebrow">{{ producto()!.categoriaNombre }} · {{ producto()!.codigo }}</span><h1>{{ producto()!.modelo }}</h1><div class="price">S/ {{ producto()!.precio | number:'1.2-2' }}</div><p class="description">{{ producto()!.descripcion || 'Consulta las características y disponibilidad de este modelo.' }}</p><div class="meta"><span>Color</span><strong>{{ producto()!.color }}</strong></div><div class="meta"><span>Disponibilidad</span><strong>{{ producto()!.cantidad > 0 ? 'En stock' : 'Agotado' }}</strong></div><button class="add-button" (click)="agregar()" [disabled]="!producto()!.cantidad">Agregar al carrito <span>→</span></button>@if (mensaje()) {<p class="notice">{{ mensaje() }}</p>}<p class="note">Pago de demostración disponible desde tu carrito y tus pedidos.</p></div></section>
    }
  </div>
`, styles: [`:host{display:block}.detail-page{padding:1.6rem 0 3rem;color:#080808}.breadcrumb{display:flex;gap:.65rem;color:#777;font-size:.85rem;margin-bottom:2.2rem}.breadcrumb a{color:inherit;text-decoration:none}.product-detail{display:grid;grid-template-columns:1fr 1fr;gap:2.2rem;align-items:start}.visual{height:500px;border-radius:22px;background:#f1f1f1;display:grid;place-items:center;overflow:hidden}.visual img{width:100%;height:100%;object-fit:contain;mix-blend-mode:multiply}.placeholder{display:grid;justify-items:center;gap:.6rem;color:#999}.placeholder b{font-size:3rem;font-weight:400}.info{padding:1.1rem .5rem}.eyebrow{font-size:.7rem;letter-spacing:.14em;color:#777;font-weight:700}.info h1{font-size:clamp(2.5rem,4.2vw,4rem);line-height:1;letter-spacing:-.065em;margin:1rem 0}.price{font-size:2rem;font-weight:750}.description{line-height:1.65;color:#777;border-bottom:1px solid #e5e5e5;padding:1rem 0 1.5rem}.meta{padding:.9rem 0;border-bottom:1px solid #e5e5e5;display:flex;justify-content:space-between}.meta span{color:#777}.add-button{width:100%;margin-top:1.5rem;border:0;border-radius:999px;height:54px;background:#050505;color:white;display:flex;justify-content:space-between;align-items:center;padding:0 1.4rem;font-size:.95rem;cursor:pointer}.add-button:disabled{opacity:.45;cursor:not-allowed}.note,.notice{font-size:.8rem;color:#777}.state{text-align:center;padding:5rem 1rem}.state a{color:#111}@media(max-width:700px){.product-detail{grid-template-columns:1fr;gap:1rem}.visual{height:350px}.info{padding:.5rem 0}.breadcrumb{margin-bottom:1.3rem}}` ] })
export class ProductoDetallePage implements OnInit {
  private readonly route = inject(ActivatedRoute); private readonly productos = inject(ProductosService); private readonly auth = inject(AuthService); private readonly carrito = inject(CarritoPedidoService);
  readonly producto = signal<Producto | null>(null); readonly cargando = signal(true); readonly mensaje = signal('');
  ngOnInit(): void { const id = Number(this.route.snapshot.paramMap.get('id')); this.productos.obtenerPorId(id).subscribe({ next: p => { this.producto.set(p); this.cargando.set(false); }, error: () => this.cargando.set(false) }); }
  imagenCompleta(url: string): string { return `${API_BASE.replace('/api/v1', '')}${url}`; }
  agregar(): void { const p = this.producto(); if (!p) return; if (!this.auth.estaAutenticado()) { this.mensaje.set('Inicia sesión para agregar este producto al carrito.'); return; } this.carrito.agregarItem(p.id, 1).subscribe({ next: () => this.mensaje.set('Producto agregado al carrito.'), error: () => this.mensaje.set('No se pudo agregar el producto. Inténtalo nuevamente.') }); }
}
