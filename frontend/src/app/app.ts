import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';
import { CarritoPedidoService } from './services/carrito-pedido.service';
import { ProductosService } from './services/productos.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-layout">
      @if (anuncioVisible()) { <div class="announcement">Equípate para lo que viene <a routerLink="/productos">Explora la colección</a><button aria-label="Cerrar anuncio" (click)="anuncioVisible.set(false)">×</button></div> }
      <header class="navbar">
        <div class="nav-container">
          <a routerLink="/inicio" class="logo">
            <span class="logo-text">MHM<span>.</span></span>
          </a>

          <button class="menu-toggle" (click)="menuAbierto.set(!menuAbierto())" [attr.aria-expanded]="menuAbierto()" aria-controls="main-navigation" aria-label="Abrir menú">☰</button>
          <nav id="main-navigation" [class.menu-open]="menuAbierto()" (click)="menuAbierto.set(false)" class="category-nav" aria-label="Categorías de la tienda">
            <a routerLink="/inicio" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Inicio</a>
            <a routerLink="/productos" routerLinkActive="active">Catálogo</a>
            @if (auth.tienePermiso('GESTIONAR_PRODUCTOS') || auth.tienePermiso('GESTIONAR_INVENTARIO') || auth.tienePermiso('GESTIONAR_CATALOGOS')) { <a routerLink="/inventario" routerLinkActive="active">Inventario</a> }
            @if (auth.tienePermiso('VER_REPORTES')) { <a routerLink="/reportes">Reportes</a> }
            @if (auth.esAdmin()) { <a routerLink="/usuarios" class="admin-link">Admin</a> }
          </nav>

          <label class="top-search">
            <input type="search" placeholder="Buscar mochilas, modelos y colores..." [value]="busqueda()" (input)="actualizarBusqueda($event)" (keydown.enter)="buscar()" aria-label="Buscar mochilas" />
            <button type="button" (click)="buscar()" aria-label="Buscar">⌕</button>
          </label>

          <div class="user-session">
              <a routerLink="/carrito" class="bag-button" aria-label="Abrir carrito"><svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3h2l3 12h10l3-9H6"/><circle cx="9" cy="20" r="1"/><circle cx="18" cy="20" r="1"/></svg>
                @if (carritoService.totalItemsCarrito() > 0) { <span class="cart-count">{{ carritoService.totalItemsCarrito() }}</span> }
              </a>

            @if (auth.estaAutenticado()) {
              <a routerLink="/pedidos" class="orders-link">{{ auth.rol() === 'CLIENTE' ? 'Mis pedidos' : 'Pedidos' }}</a>
              <a routerLink="/favoritos" class="orders-link">Favoritos</a>
              <a routerLink="/perfil" class="account-pill"><span class="avatar">{{ auth.rol() === 'CLIENTE' ? 'C' : 'M' }}</span><span class="user-email">{{ auth.email() }}</span></a>
              <button (click)="auth.cerrarSesion()" class="btn-logout" title="Cerrar sesión">Salir</button>
            } @else {
              <a routerLink="/login" class="orders-link">Iniciar sesión</a>
              <a routerLink="/login" [queryParams]="{ registro: 'cliente' }" class="btn-login">Crear cuenta</a>
            }
          </div>
        </div>
      </header>

      <main class="main-content">
        <router-outlet></router-outlet>
      </main>

      <footer class="footer">
        <section class="newsletter"><div><h2>Recibe novedades<br>para tu próxima ruta</h2><p>Ofertas y nuevos modelos de MHM Store en tu correo.</p></div><form (submit)="$event.preventDefault()"><input type="email" placeholder="Tu correo electrónico" aria-label="Tu correo electrónico"><button type="submit">Suscribirme</button></form></section>
        <div class="footer-grid"><div class="footer-brand"><a routerLink="/inicio">MHM<span>.</span></a><p>Mochilas pensadas para acompañarte en cada día, viaje y aventura.</p></div><div><h3>TIENDA</h3><a routerLink="/productos">Catálogo</a><a routerLink="/productos">Novedades</a></div><div><h3>AYUDA</h3><a routerLink="/pedidos">Mis pedidos</a><a routerLink="/carrito">Carrito</a></div><div><h3>CUENTA</h3><a routerLink="/login">Iniciar sesión</a><a routerLink="/login" [queryParams]="{ registro: 'cliente' }">Crear cuenta</a></div></div>
        <div class="footer-bottom"><span>© 2026 MHM Store. Todos los derechos reservados.</span><span>Pago de demostración disponible</span></div>
      </footer>
    </div>
  `,
  styles: [`
    .app-layout { min-height: 100vh; display: flex; flex-direction: column; background: #fff; color: #080808; }
    .announcement { position: relative; display:flex; justify-content:center; align-items:center; gap:.35rem; min-height:38px; padding:.45rem 2.5rem; background:#050505; color:#fff; font-size:.72rem; }
    .announcement a { color:#fff; text-underline-offset:3px; font-weight:600; }
    .announcement button { position:absolute; right:max(24px,calc((100vw - 1240px)/2)); top:5px; border:0; background:none; color:#fff; font-size:1.35rem; cursor:pointer; }
    .navbar { position: sticky; top: 0; z-index: 100; width: min(1240px, calc(100% - 48px)); margin: 0 auto; background: #fff; border: 0; border-bottom: 1px solid #e8e8e8; border-radius: 0; box-shadow: none; }
    .nav-container { max-width: 1240px; margin: 0 auto; padding: 1.25rem 0; display: flex; align-items: center; justify-content: space-between; gap: 1.15rem; }
    .logo { display: flex; align-items: center; text-decoration: none; color: #050505; font-weight: 950; font-size: 1.72rem; letter-spacing: -.075em; white-space: nowrap; }
    .logo-text span,.footer-brand span { color:#050505; }
    .top-search { flex: 1 1 440px; display: flex; align-items: center; gap: .5rem; height: 46px; padding: 0 .9rem 0 1.2rem; background: #f1f1f1; border: 0; border-radius: 999px; }
    .top-search input { flex: 1; min-width: 0; border: 0; outline: 0; background: transparent; color: var(--ink); font-size: .72rem; }
    .top-search input::placeholder { color: #8b908c; }
    .top-search button { width: 34px; height: 34px; display: grid; place-items: center; border: 0; border-radius: 50%; background: transparent; color: #777; font-size: 1.25rem; line-height: 1; cursor: pointer; }
    .user-session { display: flex; align-items: center; gap: .65rem; }
    .orders-link { color: #555b58; text-decoration: none; font-size: .72rem; font-weight: 700; white-space: nowrap; }
    .bag-button { position: relative; display: grid; place-items: center; width: 36px; height: 36px; border-radius: 50%; background: transparent; color:#080808; text-decoration: none; font-size: 1.1rem; }
    .cart-count { display: inline-grid; place-items: center; min-width: 20px; height: 20px; background: var(--coral); color: white; border-radius: 999px; font-size: .68rem; font-weight: 700; padding: 0 .3rem; margin-left: .25rem; }
    .bag-button .cart-count { position: absolute; top: -5px; right: -6px; min-width: 17px; height: 17px; font-size: .58rem; }
    .admin-link { color: var(--coral-deep) !important; }
    .user-session { display: flex; align-items: center; gap: .55rem; }
    .account-pill { display: flex; align-items: center; gap: .45rem; min-width: 0; }
    .account-pill { text-decoration:none; }
    .user-email{display:none!important}.bag-button{background:#050505!important;color:white!important;border-radius:12px!important;flex-shrink:0;transition:background .2s}.bag-button:hover{background:#333!important}.menu-toggle{display:none;border:0;background:#f1f1f1;border-radius:9px;padding:.55rem;font-size:1.1rem;cursor:pointer}.top-search{min-width:160px}.category-nav{flex-shrink:0}.user-session{flex-shrink:0}
    .avatar { display: grid; place-items: center; width: 32px; height: 32px; background: #d8e0dc; color: #39453e; border-radius: 50%; font-size: .72rem; font-weight: 800; }
    .user-email { color: #555b5e; font-weight: 500; max-width: 125px; overflow: hidden; text-overflow: ellipsis; font-size: .68rem; }
    .btn-logout, .btn-login { border: 0; border-radius: 999px; padding: .7rem 1.05rem; cursor: pointer; font-size: .82rem; color: white; background: var(--ink); font-weight: 700; text-decoration: none; white-space: nowrap; }
    .btn-logout:hover, .btn-login:hover { background: var(--coral-deep); }
    .category-nav { display: flex; align-items: center; gap: .9rem; min-width: 0; overflow-x: auto; }
    .category-nav a { text-decoration: none; white-space: nowrap; padding: .3rem 0; color: #333; font-size: .78rem; font-weight: 750; transition: .18s; }
    .category-nav a:hover, .category-nav a.active { color: #050505; text-decoration:underline; text-underline-offset:5px; }
    .category-nav .nav-spacer { flex: 1; min-width: .5rem; }
    .main-content { flex: 1; width: min(1240px, calc(100% - 48px)); margin: 0 auto; padding: 0 0 4rem; background: #fff; border: 0; border-radius: 0; box-shadow: none; }
    .footer { margin-top:5rem; padding:0 max(24px,calc((100vw - 1240px)/2)); background:#f0f0f0; color:#737373; font-size:.82rem; }
    .newsletter { position:relative; top:-2.4rem; margin-bottom:-.2rem; padding:2.25rem 3rem; border-radius:22px; background:#050505; color:#fff; display:flex; justify-content:space-between; align-items:center; gap:2rem; }
    .newsletter h2 { margin:0; font-size:clamp(1.7rem,3.2vw,2.55rem); line-height:1.05; font-weight:950; letter-spacing:-.055em; text-transform:uppercase; }
    .newsletter p { color:#b9b9b9; margin:.65rem 0 0; }
    .newsletter form { width:min(390px,45%); display:grid; gap:.65rem; }
    .newsletter input,.newsletter button { width:100%; height:46px; border:0; border-radius:999px; padding:0 1.1rem; }
    .newsletter button { background:#fff; color:#050505; font-weight:650; cursor:pointer; }
    .footer-grid { display:grid; grid-template-columns:2fr repeat(3,1fr); gap:2rem; padding:0 0 2.5rem; }
    .footer-brand a { font-size:1.8rem; color:#050505; font-weight:950; letter-spacing:-.07em; text-decoration:none; }
    .footer-brand p { max-width:250px; line-height:1.65; }
    .footer-grid h3 { font-size:.75rem; letter-spacing:.16em; color:#111; margin:.5rem 0 1rem; }
    .footer-grid>div:not(:first-child) { display:flex; flex-direction:column; gap:.75rem; }
    .footer-grid a:not(.footer-brand a) { color:#777; text-decoration:none; }
    .footer-bottom { display:flex; justify-content:space-between; border-top:1px solid #d6d6d6; padding:1.25rem 0; font-size:.72rem; }
    @media (max-width: 1100px) { .nav-container { flex-wrap: wrap; } .category-nav { order:3;flex:1 1 100%; } .top-search { order:4; flex:1 1 100%; } .user-session { order:2;margin-left:auto; } }
    @media (max-width: 700px) { .newsletter { padding:1.5rem; flex-direction:column; align-items:stretch; } .newsletter form { width:100%; } .footer-grid { grid-template-columns:1fr 1fr; } }
    @media(max-width:700px){.menu-toggle{display:block;order:0}.logo{order:1}.category-nav{display:none!important}.category-nav.menu-open{display:flex!important;flex-wrap:wrap;padding:.8rem;background:#fafafa;border-radius:12px}.category-nav a{padding:.5rem}.btn-login{font-size:.72rem;padding:.7rem}.btn-logout{display:none}.nav-container{gap:.6rem}.user-session{gap:.35rem}}
    @media (max-width: 560px) { .announcement { font-size:.64rem; } .navbar,.main-content { width:calc(100% - 32px); } .nav-container { padding:.8rem 0; } .logo { font-size:1.5rem; } .category-nav { padding:0 0 .55rem; } .category-nav a { padding:.48rem .68rem; } .user-email,.orders-link { display:none; } .main-content { padding:0 0 2rem; } .footer { padding:0 16px; } .footer-bottom { flex-direction:column; gap:.5rem; } }
  `]
})
export class App implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly carritoService = inject(CarritoPedidoService);
  protected readonly productos = inject(ProductosService);
  private readonly router = inject(Router);
  readonly menuAbierto = signal(false);
  readonly anuncioVisible = signal(true);
  readonly busqueda = signal('');

  ngOnInit(): void { this.productos.cargarCategorias(); }

  actualizarBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  buscar(): void {
    const query = this.busqueda().trim();
    this.router.navigate(['/productos'], { queryParams: query ? { q: query } : {} });
  }
}
