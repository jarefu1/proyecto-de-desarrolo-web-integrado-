import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductosService } from '../../services/productos.service';
import { API_BASE } from '../../config/api.config';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="home">
      <section class="hero">
        <div class="hero-copy">
          <span class="eyebrow">MHM STORE · DISEÑO PARA LLEVAR</span>
          <h1>LISTA PARA<br>LO QUE VIENE.</h1>
          <p>Mochilas funcionales para tus días, tus viajes y cada nuevo camino.</p>
          <a routerLink="/productos" class="black-button">Explorar mochilas <span>→</span></a>
        </div>
        <div class="hero-space" aria-hidden="true"><span>✦</span><i>✦</i></div>
      </section>

      @if (productos.categorias().length) {
        <nav class="category-strip" aria-label="Explorar categorías">
          @for (categoria of productos.categorias(); track categoria.id) {
            <a [routerLink]="['/productos']" [queryParams]="{ categoria: categoria.id }">{{ categoria.nombre }}</a>
          }
        </nav>
      }

      <section class="collection-section">
        <div class="section-heading"><div><span class="eyebrow">HECHAS PARA ACOMPAÑARTE</span><h2>COLECCIÓN MHM</h2></div><a routerLink="/productos" class="text-link">Ver catálogo <span>→</span></a></div>
        @if (productos.cargando()) { <p class="empty">Cargando catálogo…</p> }
        @else if (productos.productos().length) {
          <div class="product-grid">
            @for (producto of productos.productos() | slice:0:4; track producto.id) {
              <a class="product-card" [routerLink]="['/producto', producto.id]">
                <div class="product-image">
                  @if (producto.imagenUrl) { <img [src]="imagenCompleta(producto.imagenUrl)" [alt]="producto.modelo" /> }
                  @else { <span class="placeholder"><b>▧</b>Imagen no disponible</span> }
                </div>
                <h3>{{ producto.modelo }}</h3><p>{{ producto.color }} · {{ producto.categoriaNombre }}</p><strong>S/ {{ producto.precio | number:'1.2-2' }}</strong>
              </a>
            }
          </div>
        } @else { <div class="empty">Aún no hay productos en el catálogo. Las mochilas aparecerán aquí cuando se registren desde administración.</div> }
        <a routerLink="/productos" class="outline-button">Ver todo el catálogo</a>
      </section>

      @if (productos.categorias().length) {
        <section class="category-section">
          <h2>ENCUENTRA LA TUYA</h2>
          <div class="category-grid">
            @for (categoria of productos.categorias(); track categoria.id) {
              <a [routerLink]="['/productos']" [queryParams]="{ categoria: categoria.id }"><span>{{ categoria.nombre }}</span><b>↗</b></a>
            }
          </div>
        </section>
      }
    </div>
  `,
  styles: [`
    :host{display:block;color:#080808}.home{max-width:1240px;margin:auto}.hero{min-height:510px;display:grid;grid-template-columns:1fr 1fr;align-items:center;padding:4.25rem 0 4.5rem}.hero-copy{max-width:610px}.eyebrow{font-size:.7rem;letter-spacing:.17em;font-weight:700;color:#777}.hero h1{font-size:clamp(3.5rem,7.8vw,6.8rem);line-height:.88;letter-spacing:-.07em;margin:1.1rem 0 1.25rem;font-weight:950}.hero p{max-width:490px;font-size:1rem;color:#777;line-height:1.55;margin:0 0 1.7rem}.black-button{height:50px;min-width:190px;padding:0 1.4rem;border-radius:999px;background:#050505;color:white;text-decoration:none;display:inline-flex;align-items:center;justify-content:space-between;gap:1rem;font-size:.85rem}.hero-space{height:100%;position:relative;display:grid;place-items:center}.hero-space span{font-size:6rem;line-height:1;transform:translate(100px,-70px)}.hero-space i{font-style:normal;font-size:3rem;position:absolute;left:10%;top:62%}.category-strip{display:flex;justify-content:space-around;gap:1rem;padding:1.2rem 0;border-top:1px solid #e5e5e5;border-bottom:1px solid #e5e5e5;overflow-x:auto}.category-strip a{white-space:nowrap;text-decoration:none;color:#444;font-size:1.1rem;font-weight:650}.collection-section{padding:5.4rem 0 4.8rem;text-align:center}.section-heading{display:flex;align-items:end;justify-content:space-between;text-align:left;margin-bottom:2rem}.section-heading h2,.category-section h2{font-size:clamp(2rem,4vw,3rem);letter-spacing:-.055em;margin:.45rem 0 0;font-weight:950}.text-link{color:#111;font-size:.85rem;text-decoration:none}.product-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:1.25rem;text-align:left}.product-card{color:inherit;text-decoration:none;min-width:0}.product-image{height:300px;background:#f0f0ef;border-radius:20px;overflow:hidden;display:grid;place-items:center;margin-bottom:1rem}.product-image img{width:100%;height:100%;object-fit:contain;mix-blend-mode:multiply}.placeholder{display:grid;justify-items:center;gap:.5rem;color:#929292;font-size:.8rem}.placeholder b{font-size:2.2rem;font-weight:400}.product-card h3{font-size:1.05rem;margin:0 0 .45rem;letter-spacing:-.02em}.product-card p{font-size:.82rem;color:#777;margin:0 0 .55rem}.product-card strong{font-size:1.25rem}.outline-button{display:inline-flex;align-items:center;justify-content:center;min-width:200px;padding:.85rem 1.5rem;margin-top:2rem;border:1px solid #ddd;border-radius:999px;color:#111;text-decoration:none;font-size:.82rem}.category-section{padding:3.2rem 3rem 3.5rem;background:#f1f1f1;border-radius:28px;text-align:center}.category-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1rem;margin-top:2rem}.category-grid a{min-height:150px;padding:1.5rem 1.7rem;border-radius:20px;background:#fff;display:flex;justify-content:space-between;align-items:flex-start;text-decoration:none;color:#111;font-size:1.7rem;font-weight:700}.category-grid b{font-size:1rem}@media(max-width:850px){.hero{min-height:420px}.product-image{height:230px}.product-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.hero-space span{transform:translate(25px,-40px)}}@media(max-width:560px){.hero{min-height:380px;grid-template-columns:1fr;padding:3.5rem 0}.hero-space{display:none}.hero h1{font-size:clamp(3.3rem,16vw,5rem)}.category-strip{justify-content:flex-start}.category-strip a{font-size:.9rem}.collection-section{padding:3.5rem 0}.section-heading{align-items:start;gap:1rem}.section-heading h2{font-size:1.8rem}.text-link{white-space:nowrap;margin-top:1.2rem}.product-grid{gap:.8rem}.product-image{height:175px;border-radius:14px}.product-card h3{font-size:.9rem}.product-card p{font-size:.72rem}.category-section{padding:2rem 1rem;border-radius:20px}.category-grid{gap:.7rem}.category-grid a{min-height:105px;padding:1rem;font-size:1.1rem;border-radius:15px}}
  `]
})
export class InicioPage implements OnInit {
  protected readonly productos = inject(ProductosService);
  ngOnInit(): void { this.productos.cargarTodo(); }
  imagenCompleta(url: string): string { return `${API_BASE.replace('/api/v1', '')}${url}`; }
}
