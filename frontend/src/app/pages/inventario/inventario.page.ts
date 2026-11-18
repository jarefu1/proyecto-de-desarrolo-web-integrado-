import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductosService } from '../../services/productos.service';
import { AuthService } from '../../services/auth.service';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { Producto, ProductoRequest } from '../../models/producto.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { API_BASE } from '../../config/api.config';
import { FavoritosService } from '../../services/favoritos.service';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page-container">
      <header class="page-header">
        <div>
          <span class="eyebrow">MHM STORE · HECHAS PARA TU CAMINO</span>
          <h2>Control de inventario</h2>
          <p class="subtitle">Administra productos, precios, existencias e imágenes de la tienda.</p>
        </div>
        <div class="header-actions">
          <label class="search-box"><span>⌕</span><input type="search" placeholder="Busca tu modelo..." [value]="busqueda()" (input)="buscar($event)" /></label>
          @if (auth.tienePermiso('GESTIONAR_INVENTARIO')) {
            <button (click)="alternarFiltroStockBajo()" [class.active]="filtroStockBajo()" class="btn btn-warning">{{ filtroStockBajo() ? 'Todo el stock' : 'Stock bajo' }}</button>
          }
          @if (auth.tienePermiso('GESTIONAR_PRODUCTOS')) {
            <button (click)="abrirModalCrear()" class="btn btn-primary">+ Añadir mochila</button>
          }
        </div>
      </header>

      <section class="inventory-stats">
        @for (dato of indicadores(); track dato.nombre) { <article [class]="dato.clase"><small>{{ dato.nombre }}</small><strong>{{ dato.valor }}</strong></article> }
      </section>
      @if (prods.error()) { <div class="alert alert-error">{{ prods.error() }}</div> }
      <label class="availability-filter">Disponibilidad <select [value]="disponibilidad()" (change)="disponibilidad.set(valor($event)); cargarProductos(0)"><option value="">Todas</option><option value="true">Con existencias</option><option value="false">Agotados</option></select></label>
      @if (auth.tienePermiso('GESTIONAR_CATALOGOS')) {
        <details class="category-management"><summary>Gestión de categorías</summary>
          <form (submit)="$event.preventDefault(); guardarCategoria()" class="category-editor">
            <label>Nombre <input required maxlength="60" [value]="nombreCategoria()" (input)="nombreCategoria.set(valor($event))" /></label>
            <button type="submit" [disabled]="!nombreCategoria().trim() || categoriaOcupada()" class="btn btn-primary">{{ categoriaEditando() ? 'Guardar cambios' : 'Agregar categoría' }}</button>
            @if (categoriaEditando()) { <button type="button" class="btn btn-secondary" (click)="categoriaEditando.set(null); nombreCategoria.set('')">Cancelar</button> }
          </form>
          <div class="category-list">@for (cat of categoriasAdmin(); track cat.id) {
            <div><span><b>{{ cat.nombre }}</b> · {{ cat.productos }} productos</span><button class="btn" (click)="categoriaEditando.set(cat.id); nombreCategoria.set(cat.nombre)">Editar</button><button class="btn danger" [disabled]="cat.productos > 0 || categoriaOcupada()" title="Solo se pueden eliminar categorías sin productos" (click)="eliminarCategoria(cat.id, cat.nombre)">Eliminar</button></div>
          }</div>
        </details>
      }
      <nav class="category-pills" aria-label="Filtrar mochilas por categoría">
        <button (click)="seleccionarCategoria(null)" [class.selected]="categoriaSeleccionada() === null">Todas</button>
        @for (categoria of prods.categorias(); track categoria.id) {
          <button (click)="seleccionarCategoria(categoria.id)" [class.selected]="categoriaSeleccionada() === categoria.id">{{ categoria.nombre }}</button>
        }
        
      </nav>

      @if (mensajeExito()) {
        <div class="alert alert-success">{{ mensajeExito() }}</div>
      }
      @if (mensajeError()) {
        <div class="alert alert-error">{{ mensajeError() }}</div>
      }

      <!-- Grid de productos -->
      @if (prods.cargando()) {
        <div class="loading">Cargando mochilas desde el servidor...</div>
      } @else if (prods.productos().length === 0) {
        <div class="empty-state">No hay mochilas registradas con este criterio.</div>
      } @else {
        <div class="products-grid">
          @for (prod of prods.productos(); track prod.id) {
            <div class="product-card" [class.low-stock]="prod.cantidad > 0 && prod.cantidad <= prod.stockMinimo">
              <div class="card-badge" [class.badge-empty]="prod.cantidad === 0" [class.badge-warning]="prod.cantidad > 0 && prod.cantidad <= prod.stockMinimo" [class.badge-success]="prod.cantidad > prod.stockMinimo">
                {{ estadoStock(prod) }} · {{ prod.cantidad }}
              </div>
              <div class="card-header">
                <span class="sku-tag">{{ prod.codigo }}</span>
                <span class="category-tag">{{ prod.categoriaNombre }}</span>
              </div>
              <div class="product-art">
                @if (prod.imagenUrl) { <img [src]="imagenCompleta(prod.imagenUrl)" [alt]="prod.modelo" (error)="prod.imagenUrl=null" /> }
                @else { <span class="image-placeholder"><span>▧</span>Imagen no disponible</span> }
              </div>
              <h3 class="product-title">{{ prod.modelo }}</h3>
              <a class="detail-link" [routerLink]="['/producto', prod.id]">Ver detalles <span>→</span></a>
              <p class="product-meta">{{ prod.activo ? 'Publicado' : 'Oculto' }} · Mínimo: {{ prod.stockMinimo }}</p><p class="product-meta">Creado: {{ prod.fechaCreacion | date:'short' }} · Actualizado: {{ prod.fechaActualizacion | date:'short' }}</p>
              <p class="product-meta">Color: <strong>{{ prod.color }}</strong> | Almacén: <strong>{{ prod.almacenNombre }}</strong></p>
              <div class="product-price">S/ {{ prod.precio | number:'1.2-2' }}</div>

              <div class="card-actions">
                @if (auth.tienePermiso('GESTIONAR_INVENTARIO')) {
                  <div class="stock-controls">
                    <button (click)="ajustarStock(prod, 'entrada')" class="btn-icon" title="Entrada rápida de stock (+1)">➕</button>
                    <button (click)="ajustarStock(prod, 'salida')" class="btn-icon" title="Salida rápida de stock (-1)" [disabled]="prod.cantidad <= 0">➖</button>
                  </div>
                }

                @if (auth.tienePermiso('GESTIONAR_PRODUCTOS')) {
                  <div class="admin-controls">
                    <button (click)="abrirModalEditar(prod)" class="btn-icon edit" title="Editar">✏️</button>
                    <button (click)="eliminarProducto(prod)" class="btn-icon delete" title="Eliminar">🗑️</button>
                  </div>
                }
              </div>
            </div>
          }
        </div>
      }

      <div class="pagination" aria-label="Paginación de productos">
        <button class="btn btn-secondary" (click)="cambiarPagina(-1)" [disabled]="prods.paginaActual() === 0 || prods.cargando()">Anterior</button>
        <span>Página {{ prods.paginaActual() + 1 }} de {{ prods.totalPaginas() || 1 }}</span>
        <button class="btn btn-secondary" (click)="cambiarPagina(1)" [disabled]="prods.paginaActual() + 1 >= prods.totalPaginas() || prods.cargando()">Siguiente</button>
      </div>

      <!-- Modal Crear / Editar -->
      @if (mostrarModal()) {
        <div class="modal-backdrop">
          <div class="modal-dialog">
            <div class="modal-header">
              <h3>{{ editandoId() ? 'Editar Mochila' : 'Registrar Nueva Mochila' }}</h3>
              <button (click)="cerrarModal()" class="close-btn">&times;</button>
            </div>

            <form [formGroup]="form" (ngSubmit)="guardarProducto()">
              <div class="form-grid">
                <div class="form-group">
                  <label>Código SKU *</label>
                  <input formControlName="codigo" placeholder="Ej. MOCH015" />
                  @if (form.get('codigo')?.invalid && form.get('codigo')?.touched) {
                    <small class="error">Código obligatorio (máx. 12 car.)</small>
                  }
                </div>

                <div class="form-group">
                  <label>Modelo *</label>
                  <input formControlName="modelo" placeholder="Ej. Mochila Urbana Pro" />
                  @if (form.get('modelo')?.invalid && form.get('modelo')?.touched) {
                    <small class="error">Modelo obligatorio</small>
                  }
                </div>

                <div class="form-group">
                  <label>Color *</label>
                  <input formControlName="color" placeholder="Ej. Negro" />
                  @if (form.get('color')?.invalid && form.get('color')?.touched) {
                    <small class="error">Color obligatorio</small>
                  }
                </div>

                <div class="form-group span-2">
                  <label>Descripción</label>
                  <textarea formControlName="descripcion" rows="3" maxlength="255" placeholder="Detalles y características de la mochila"></textarea>
                </div>

                <div class="form-group"><label>Stock mínimo *</label><input type="number" min="0" step="1" formControlName="stockMinimo" /></div>
                <div class="form-group"><label>Estado *</label><select formControlName="activo"><option [ngValue]="true">Publicado</option><option [ngValue]="false">Oculto</option></select></div>
                <div class="form-group">
                  <label>Categoría *</label>
                  <select formControlName="categoriaId">
                    <option [ngValue]="null">Seleccione...</option>
                    @for (cat of prods.categorias(); track cat.id) {
                      <option [ngValue]="cat.id">{{ cat.nombre }}</option>
                    }
                  </select>
                </div>

                <div class="form-group">
                  <label>Precio (S/) *</label>
                  <input type="number" step="0.01" formControlName="precio" />
                  @if (form.get('precio')?.invalid && form.get('precio')?.touched) {
                    <small class="error">Precio debe ser mayor a 0</small>
                  }
                </div>

                <div class="form-group">
                  <label>Cantidad (Stock) *</label>
                  <input type="number" formControlName="cantidad" />
                  @if (form.get('cantidad')?.invalid && form.get('cantidad')?.touched) {
                    <small class="error">Cantidad no puede ser negativa</small>
                  }
                </div>

                <div class="form-group span-2">
                  <label>Almacén *</label>
                  <select formControlName="almacenId">
                    <option [ngValue]="null">Seleccione...</option>
                    @for (alm of prods.almacenes(); track alm.id) {
                      <option [ngValue]="alm.id">{{ alm.nombre }} - {{ alm.ubicacion }}</option>
                    }
                  </select>
                </div>
              </div>

              <div class="image-upload">
                <label for="product-image">Imagen del producto</label>
                @if (imagenPrevia() || imagenActual()) {
                  <img class="image-preview" [src]="imagenPrevia() || imagenCompleta(imagenActual()!)" alt="Vista previa de la imagen" />
                } @else { <div class="image-preview image-placeholder">▧<small>Sin imagen</small></div> }
                <input id="product-image" type="file" accept="image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp" (change)="seleccionarImagen($event)" />
                <small>JPG, PNG o WebP · máximo 5 MB. Si editas y no eliges otra, se conserva la actual.</small>
                @if (errorImagen()) { <small class="error">{{ errorImagen() }}</small> }
              </div>

              <div class="modal-footer">
                <button type="button" (click)="cerrarModal()" class="btn btn-secondary">Cancelar</button>
                <button type="submit" [disabled]="form.invalid || guardando()" class="btn btn-primary">
                  {{ guardando() ? 'Guardando...' : 'Guardar' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .inventory-stats{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:.8rem;margin-bottom:1.5rem}.inventory-stats article{padding:1.2rem;background:#f5f5f5;border:1px solid #e6e6e6;border-radius:16px;display:grid;gap:.4rem}.inventory-stats small{color:#666}.inventory-stats strong{font-size:2rem}.inventory-stats .green{background:#ecf8f0;color:#17723b}.inventory-stats .orange{background:#fff3e7;color:#ad5609}.inventory-stats .red{background:#fff0f0;color:#bb3030}.inventory-stats .yellow{background:#fff8d3}.category-management{padding:1rem;border:1px solid #ddd;border-radius:15px;margin:1rem 0}.category-management summary{font-weight:750;cursor:pointer}.category-editor{display:flex;gap:.7rem;align-items:end;margin:1rem 0;flex-wrap:wrap}.category-editor label{display:grid;gap:.4rem}.category-editor input,.availability-filter select{padding:.6rem;border:1px solid #ccc;border-radius:9px}.category-list>div{display:flex;gap:.5rem;align-items:center;padding:.5rem;border-top:1px solid #eee;flex-wrap:wrap}.category-list span{flex:1}.danger{color:#ba2525!important}.availability-filter{font-size:.85rem;display:flex;align-items:center;gap:.7rem;margin-bottom:1rem}.badge-empty{background:#ffe5e5!important;color:#b22525!important}.page-container { padding: 1.5rem 0; }
    @media(max-width:700px){.inventory-stats{grid-template-columns:repeat(2,minmax(0,1fr))}.form-grid{grid-template-columns:1fr!important}.span-2{grid-column:auto!important}.modal-dialog{width:calc(100% - 24px)!important}}


















    .stock-controls, .admin-controls { display: flex; gap: 0.25rem; }
    .btn-icon { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.4rem; cursor: pointer; transition: background 0.15s; }
    .btn-icon:hover { background: #e2e8f0; }

    .modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-dialog { background: white; border-radius: 12px; width: 100%; max-width: 550px; padding: 1.5rem; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; }
    .modal-header h3 { margin: 0; color: #1e293b; }
    .close-btn { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #94a3b8; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .span-2 { grid-column: span 2; }
    .form-group label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.35rem; color: #334155; }
    .form-group input, .form-group select { width: 100%; padding: 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box; }
    .error { color: #ef4444; font-size: 0.75rem; margin-top: 0.2rem; display: block; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }

    .alert { padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.5rem; font-size: 0.9rem; }
    .alert-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
    .alert-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }







    .page-container { padding: .4rem 0 1rem; }
    .page-header { align-items: end; margin-bottom: 1.25rem; gap: 1.25rem; }
    .page-header h2 { font-size: clamp(1.75rem, 3vw, 2.5rem); letter-spacing: -.07em; margin-top: .45rem; color: var(--ink); }
    .eyebrow { color: #828783; font-size: .62rem; letter-spacing: .15em; font-weight: 800; }
    .subtitle { color: #777d79; font-size: .86rem; }
    .header-actions { align-items: center; gap: .55rem; }
    .search-box { display: flex; align-items: center; gap: .55rem; min-width: 220px; padding: .65rem .85rem; border: 1px solid rgba(33,38,38,.09); border-radius: 999px; background: rgba(255,255,255,.72); }
    .search-box span { font-size: 1.25rem; color: #858a86; line-height: .8; }
    .search-box input { width: 100%; border: 0; outline: 0; background: transparent; font-size: .75rem; color: var(--ink); }
    .category-pills { display: flex; align-items: center; gap: .45rem; overflow-x: auto; padding: .1rem 0 1rem; margin-bottom: 1rem; border-bottom: 1px solid var(--line); }
    .category-pills button { flex: 0 0 auto; border: 1px solid transparent; border-radius: 999px; background: rgba(255,255,255,.66); padding: .58rem 1rem; color: #777d79; font-size: .72rem; font-weight: 700; cursor: pointer; transition: .18s; }
    .category-pills button:hover, .category-pills button.selected { background: #1a1d1e; color: white; }
    .products-grid { grid-template-columns: repeat(auto-fill, minmax(225px, 1fr)); gap: 1rem; }
    .product-card { padding: .8rem; border: 1px solid rgba(255,255,255,.85); border-radius: 19px; background: rgba(255,255,255,.76); box-shadow: 0 8px 28px rgba(40,47,44,.055); overflow: hidden; }
    .product-card:hover { transform: translateY(-4px); box-shadow: 0 16px 34px rgba(40,47,44,.13); }
    .card-header { align-items: center; margin: .15rem 0 .65rem; }
    .sku-tag { background: #f0f0ed; color: #6f7671; font-size: .62rem; border-radius: 999px; }
    .category-tag { background: #e3e9e4; color: #53625a; font-size: .62rem; border-radius: 999px; }
    .product-art { height: 230px; display: grid; place-items: center; overflow: hidden; border-radius: 18px; margin-bottom: .9rem; background: #f0f0ef; }
    .product-art img { width: 100%; height: 100%; object-fit: contain; mix-blend-mode: multiply; }
    .image-placeholder { display: grid; place-items: center; gap: .35rem; color: #929491; font-size: .78rem; }
    .image-placeholder span { font-size: 2rem; }
    .image-upload { display: grid; gap: .6rem; margin-top: 1rem; }
    .image-preview { width: 180px; height: 150px; object-fit: contain; border-radius: 14px; background: #f0f0ef; }
    .image-preview.image-placeholder { font-size: 2rem; }
    .card-badge { top: 1.05rem; right: 1.05rem; z-index: 3; padding: .3rem .55rem; font-size: .59rem; background: rgba(255,255,255,.83); color: #59615b; backdrop-filter: blur(8px); }
    .badge-warning { color: #a56348; background: #fff4e7; }
    .badge-success { color: #536259; background: #e7eee8; }
    .product-title { color: var(--ink); font-size: 1rem; letter-spacing: -.035em; margin-bottom: .25rem; }
    .detail-link { display:flex; justify-content:space-between; color:#111; font-size:.76rem; text-decoration:none; border-top:1px solid #eee; padding:.7rem 0; margin-bottom:.25rem; }
    .product-meta { color: #818783; font-size: .7rem; margin-bottom: .7rem; }
    .product-price { color: var(--ink); font-size: 1.15rem; margin-bottom: .8rem; }
    .card-actions { border-color: var(--line); padding-top: .65rem; gap: .5rem; }
    .btn { border-radius: 999px; font-size: .72rem; }
    .btn-primary { background: #191c1d; color: white; }
    .btn-primary:hover { background: var(--coral-deep); }
    .btn-warning { color: #665b53; background: #eee8df; }
    .btn-warning.active { color: white; background: #9e6654; }
    .btn-secondary { background: rgba(255,255,255,.8); color: #4f5652; border: 1px solid var(--line); }
    .pagination { color: #747a76; font-size: .75rem; }
    .pagination button { padding: .65rem 1rem; }
    @media (max-width: 800px) { .page-header { align-items: stretch; flex-direction: column; } .header-actions { flex-wrap: wrap; } .search-box { flex: 1; } }
    @media (max-width: 560px) { .products-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .65rem; } .product-card { padding: .55rem; border-radius: 15px; } .product-art { height: 145px; } .card-actions { flex-wrap: wrap; } .card-actions > button { width: 100%; } .collection-note { display: none; } }

 .products-grid{display:grid}.page-header,.header-actions,.card-header,.card-actions,.pagination{display:flex}.page-header{justify-content:space-between}.card-actions{justify-content:space-between;align-items:center;border-top:1px solid #eee}.card-header{gap:.5rem}.product-card{position:relative;display:flex;flex-direction:column;transition:transform .2s,box-shadow .2s}.btn{padding:.65rem 1rem;border:1px solid transparent;cursor:pointer;font-weight:650;display:inline-block;text-decoration:none}.btn:disabled{opacity:.45;cursor:not-allowed}.pagination{justify-content:center;gap:1rem;margin:1.5rem 0}.modal-dialog{max-height:90vh;overflow-y:auto}.form-group textarea{width:100%;border:1px solid #ccc;border-radius:8px;padding:.7rem;font:inherit}.sku-tag,.category-tag{padding:.25rem .45rem}.product-price{font-weight:800;margin-top:auto}.image-upload small{color:#666;font-size:.75rem}.empty-state,.loading{padding:3rem;text-align:center;border:1px dashed #ddd;border-radius:16px}
 .badge-warning{background:#fff0de;color:#a95109}.badge-success{background:#e2f6e8;color:#17763c}.product-card{border:1px solid #e8e8e8}.card-badge{position:static;align-self:flex-end;margin-bottom:.5rem}.btn-primary:hover{background:#333}.btn-warning.active{background:#a95109;color:#fff}
  `]
})
export class InventarioPage implements OnInit {
  protected readonly prods = inject(ProductosService);
  protected readonly auth = inject(AuthService);
  protected readonly favoritos = inject(FavoritosService);
  private readonly carritoService = inject(CarritoPedidoService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly indicadores = signal<{nombre: string; valor: number; clase: string}[]>([]);
  readonly categoriasAdmin = signal<import('../../services/productos.service').CategoriaInventario[]>([]);
  readonly disponibilidad = signal(''); readonly nombreCategoria = signal('');
  readonly categoriaEditando = signal<number | null>(null); readonly categoriaOcupada = signal(false);
  valor(e: Event): string { return (e.target as HTMLInputElement).value; }
  estadoStock(p: Producto): string { return p.cantidad === 0 ? 'Agotado' : p.cantidad <= p.stockMinimo ? 'Stock bajo' : 'Disponible'; }
  actualizarIndicadores(): void {
    this.prods.resumenInventario().subscribe({next: r => this.indicadores.set([{nombre:'Productos',valor:r.total,clase:''},{nombre:'Stock suficiente',valor:r.disponibles,clase:'green'},{nombre:'Stock bajo',valor:r.bajos,clase:'orange'},{nombre:'Agotados',valor:r.agotados,clase:'red'},{nombre:'Categorías',valor:r.categorias,clase:'yellow'}]), error: e => this.mensajeError.set(this.auth.mensajeError(e))});
    this.prods.categoriasInventario().subscribe({next: c => this.categoriasAdmin.set(c), error: e => this.mensajeError.set(this.auth.mensajeError(e))});
  }
  guardarCategoria(): void {
    if (!this.nombreCategoria().trim() || this.categoriaOcupada()) return;
    const cat = this.categoriasAdmin().find(c => c.id === this.categoriaEditando()); this.categoriaOcupada.set(true);
    this.prods.guardarCategoria(this.categoriaEditando(), this.nombreCategoria().trim(), cat?.descripcion ?? '').subscribe({next: () => { this.categoriaOcupada.set(false); this.categoriaEditando.set(null); this.nombreCategoria.set(''); this.actualizarIndicadores(); this.cargarProductos(0); this.mensajeExito.set('Categoría guardada.'); }, error: e => { this.categoriaOcupada.set(false); this.mensajeError.set(this.auth.mensajeError(e)); }});
  }
  eliminarCategoria(id: number, nombre: string): void {
    if (!confirm(`¿Eliminar la categoría ${nombre}?`)) return; this.categoriaOcupada.set(true);
    this.prods.eliminarCategoria(id).subscribe({next: () => { this.categoriaOcupada.set(false); this.actualizarIndicadores(); }, error: e => { this.categoriaOcupada.set(false); this.mensajeError.set(this.auth.mensajeError(e)); }});
  }
  readonly filtroStockBajo = signal(false);
  readonly categoriaSeleccionada = signal<number | null>(null);
  readonly busqueda = signal('');
  readonly mostrarModal = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');
  readonly imagenArchivo = signal<File | null>(null);
  readonly imagenPrevia = signal('');
  readonly imagenActual = signal<string | null>(null);
  readonly errorImagen = signal('');

  readonly form: FormGroup = this.fb.group({
    stockMinimo: [5, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
    activo: [true, Validators.required],
    codigo: ['', [Validators.required, Validators.maxLength(12)]],
    modelo: ['', [Validators.required, Validators.maxLength(80)]],
    color: ['', [Validators.required, Validators.maxLength(30)]],
    descripcion: ['', [Validators.maxLength(255)]],
    categoriaId: [null, [Validators.required]],
    precio: [0.01, [Validators.required, Validators.min(0.01)]],
    cantidad: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
    almacenId: [null, [Validators.required]]
  });

  ngOnInit(): void {
    const query = this.route.snapshot.queryParamMap.get('q') ?? '';
    const categoria = Number(this.route.snapshot.queryParamMap.get('categoria')) || null;
    this.busqueda.set(query);
    this.categoriaSeleccionada.set(categoria);
    this.prods.cargarTodo(query, categoria, true);
    this.actualizarIndicadores();
    if (this.auth.estaAutenticado()) this.carritoService.cargarCarritoUsuario();
    this.route.queryParamMap.subscribe((params) => {
      const nextQuery = params.get('q') ?? '';
      const nextCategory = Number(params.get('categoria')) || null;
      if (nextQuery !== this.busqueda() || nextCategory !== this.categoriaSeleccionada()) {
        this.busqueda.set(nextQuery);
        this.categoriaSeleccionada.set(nextCategory);
        this.cargarProductos(0);
      }
    });
  }

  alternarFiltroStockBajo(): void {
    const nuevoEstado = !this.filtroStockBajo();
    this.filtroStockBajo.set(nuevoEstado);
    this.cargarProductos(0);
  }

  cambiarPagina(delta: number): void {
    this.cargarProductos(this.prods.paginaActual() + delta);
  }

  seleccionarCategoria(id: number | null): void {
    this.categoriaSeleccionada.set(id);
    this.cargarProductos(0);
  }

  buscar(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
    this.cargarProductos(0);
  }

  cargarProductos(page: number): void {
    this.prods.cargarPagina(page, this.filtroStockBajo(), this.busqueda(), this.categoriaSeleccionada(), true, this.disponibilidad());
  }

  abrirModalCrear(): void {
    this.limpiarImagen();
    this.editandoId.set(null);
    this.form.reset({ precio: null, cantidad: 0, stockMinimo: 5, activo: true, categoriaId: null, almacenId: null });
    this.mostrarModal.set(true);
  }

  abrirModalEditar(p: Producto): void {
    this.limpiarImagen();
    this.imagenActual.set(p.imagenUrl);
    this.editandoId.set(p.id);
    this.form.patchValue({
      stockMinimo: p.stockMinimo, activo: p.activo,
      codigo: p.codigo,
      modelo: p.modelo,
      color: p.color,
      descripcion: p.descripcion ?? '',
      categoriaId: p.categoriaId,
      precio: p.precio,
      cantidad: p.cantidad,
      almacenId: p.almacenId
    });
    this.mostrarModal.set(true);
  }

  cerrarModal(): void {
    this.mostrarModal.set(false);
    this.editandoId.set(null);
    this.limpiarImagen();
  }

  private limpiarImagen(): void {
    if (this.imagenPrevia()) URL.revokeObjectURL(this.imagenPrevia());
    this.imagenArchivo.set(null); this.imagenPrevia.set(''); this.imagenActual.set(null); this.errorImagen.set('');
  }

  seleccionarImagen(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.errorImagen.set('');
    if (!file) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || !/\.(jpe?g|png|webp)$/i.test(file.name)) {
      this.errorImagen.set('Selecciona una imagen JPG, PNG o WebP.'); input.value = ''; return;
    }
    if (file.size > 5 * 1024 * 1024) { this.errorImagen.set('La imagen no debe superar 5 MB.'); input.value = ''; return; }
    if (this.imagenPrevia()) URL.revokeObjectURL(this.imagenPrevia());
    this.imagenArchivo.set(file); this.imagenPrevia.set(URL.createObjectURL(file)); this.imagenActual.set(null);
  }

  imagenCompleta(url: string): string { return `${API_BASE.replace('/api/v1', '')}${url}`; }

  guardarProducto(): void {
    if (this.form.invalid || this.guardando()) { this.form.markAllAsTouched(); return; }

    this.guardando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');

    const request: ProductoRequest = this.form.value;
    const id = this.editandoId();

    const finish = () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.cargarProductos(0); this.actualizarIndicadores();
        this.mensajeExito.set(id ? 'Mochila actualizada correctamente.' : 'Mochila registrada con éxito.');
        setTimeout(() => this.mensajeExito.set(''), 4000);
    };
    const guardarImagenSiCorresponde = (producto: Producto) => {
      const archivo = this.imagenArchivo();
      if (!archivo) { finish(); return; }
      this.editandoId.set(producto.id);
      this.prods.guardarImagen(producto.id, archivo).subscribe({ next: finish, error: (err) => { this.guardando.set(false); this.mensajeError.set(`Producto guardado; puedes reintentar la imagen: ${this.auth.mensajeError(err)}`); } });
    };
    const op$ = id ? this.prods.actualizar(id, request) : this.prods.crear(request);
    op$.subscribe({
      next: guardarImagenSiCorresponde,
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(this.auth.mensajeError(err));
      }
    });
  }

  eliminarProducto(p: Producto): void {
    if (!confirm(`¿Eliminar la mochila ${p.modelo} (${p.codigo})?`)) return;

    this.prods.eliminar(p.id).subscribe({
      next: () => {
        this.mensajeExito.set('Mochila eliminada correctamente.'); this.cargarProductos(0); this.actualizarIndicadores();
        setTimeout(() => this.mensajeExito.set(''), 3000);
      },
      error: (err) => this.mensajeError.set(this.auth.mensajeError(err))
    });
  }

  ajustarStock(p: Producto, tipo: 'entrada' | 'salida'): void {
    this.prods.ajustarStock(p.id, tipo, 1).subscribe({
      next: () => {
        this.actualizarIndicadores();
        this.mensajeExito.set(`Stock ajustado (${tipo === 'entrada' ? '+1' : '-1'}).`);
        setTimeout(() => this.mensajeExito.set(''), 2500);
      },
      error: (err) => this.mensajeError.set(this.auth.mensajeError(err))
    });
  }

  agregarAlCarrito(p: Producto): void {
    if (!this.auth.estaAutenticado()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/productos' } });
      return;
    }
    this.carritoService.agregarItem(p.id, 1).subscribe({
      next: () => {
        this.mensajeExito.set(`Se añadió "${p.modelo}" al carrito de compras.`);
        setTimeout(() => this.mensajeExito.set(''), 3000);
      },
      error: (err) => this.mensajeError.set(this.auth.mensajeError(err))
    });
  }

  alternarFavorito(id: number): void { this.favoritos.alternar(id); }
}

