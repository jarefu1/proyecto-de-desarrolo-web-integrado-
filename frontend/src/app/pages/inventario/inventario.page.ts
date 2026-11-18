import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductosService } from '../../services/productos.service';
import { AuthService } from '../../services/auth.service';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { Producto, ProductoRequest } from '../../models/producto.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { API_BASE } from '../../config/api.config';
import { InventarioVistaComponent } from '../../components/inventario-vista.component';
import { InventarioListaComponent } from '../../components/inventario-lista.component';
import { GaleriaEditorComponent } from '../../components/galeria-editor.component';
import { FavoritosService } from '../../services/favoritos.service';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, GaleriaEditorComponent, InventarioListaComponent, InventarioVistaComponent],
  templateUrl: './inventario.page.html',
  styleUrl: './inventario.page.css'

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
  readonly vista = signal<'lista' | 'tarjetas'>(this.leerVista());
  private leerVista(): 'lista' | 'tarjetas' { try { return localStorage.getItem('mhm_inventario_vista') === 'tarjetas' ? 'tarjetas' : 'lista'; } catch { return 'lista'; } }
  cambiarVista(vista: 'lista' | 'tarjetas'): void { this.vista.set(vista); try { localStorage.setItem('mhm_inventario_vista', vista); } catch { } }

  readonly filtroStockBajo = signal(false);
  readonly categoriaSeleccionada = signal<number | null>(null);
  readonly busqueda = signal('');
  readonly mostrarModal = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');
  readonly colorGuardado = signal('');
  readonly imagenArchivo = signal<File | null>(null);
  readonly imagenPrevia = signal('');
  readonly imagenActual = signal<string | null>(null);
  readonly errorImagen = signal('');

  readonly form: FormGroup = this.fb.group({
    colorHex: [null, Validators.pattern(/^#[0-9a-fA-F]{6}$/)], material: ['', Validators.maxLength(100)], capacidad: ['', Validators.maxLength(40)], dimensiones: ['', Validators.maxLength(80)], peso: ['', Validators.maxLength(40)],
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
    this.colorGuardado.set(p.color);
    this.imagenActual.set(p.imagenUrl);
    this.editandoId.set(p.id);
    this.form.patchValue({
      colorHex: p.colorHex ?? null, material: p.material ?? '', capacidad: p.capacidad ?? '', dimensiones: p.dimensiones ?? '', peso: p.peso ?? '',
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
    this.colorGuardado.set(''); this.imagenArchivo.set(null); this.imagenPrevia.set(''); this.imagenActual.set(null); this.errorImagen.set('');
  }

  seleccionarImagen(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.errorImagen.set('');
    if (!file) return;
    if (this.imagenPrevia()) URL.revokeObjectURL(this.imagenPrevia());
    this.imagenArchivo.set(null); this.imagenPrevia.set('');
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || !/\.(jpe?g|png|webp)$/i.test(file.name)) {
      this.errorImagen.set('Selecciona una imagen JPG, PNG o WebP.'); input.value = ''; return;
    }
    if (file.size > 5 * 1024 * 1024) { this.errorImagen.set('La imagen no debe superar 5 MB.'); input.value = ''; return; }
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

    const colorGuardado = this.form.get('color')?.value;
    const teniaImagen = !!this.imagenArchivo();
    const finish = () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.cargarProductos(0); this.actualizarIndicadores();
        this.mensajeExito.set((id ? 'Mochila actualizada.' : 'Mochila registrada.') + (teniaImagen ? ` Imagen guardada para el color ${colorGuardado}.` : ''));
        setTimeout(() => this.mensajeExito.set(''), 4000);
    };
    const guardarImagenSiCorresponde = (producto: Producto) => {
      const archivo = this.imagenArchivo();
      if (!archivo) { finish(); return; }
      this.editandoId.set(producto.id);
      this.colorGuardado.set(producto.color);
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

