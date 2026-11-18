import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductosService } from '../../services/productos.service';
import { AuthService } from '../../services/auth.service';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { FavoritosService } from '../../services/favoritos.service';
import { Producto } from '../../models/producto.model';
import { API_BASE } from '../../config/api.config';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, RouterLink],
  providers: [ProductosService],
  templateUrl: './productos.page.html',
  styleUrl: './productos.page.css'
})
export class ProductosPage implements OnInit {
  readonly prods = inject(ProductosService);
  readonly auth = inject(AuthService);
  readonly favoritos = inject(FavoritosService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly carrito = inject(CarritoPedidoService);

  readonly busqueda = signal('');
  readonly categoria = signal<number | null>(null);
  readonly precioMin = signal<number | null>(null);
  readonly precioMax = signal<number | null>(null);
  readonly disponibilidad = signal('');
  readonly mensaje = signal('');
  readonly ocupados = signal<number[]>([]);

  ngOnInit(): void {
    this.prods.cargarCategorias();
    this.route.queryParamMap.subscribe((params) => {
      this.busqueda.set(params.get('q') ?? '');
      this.categoria.set(Number(params.get('categoria')) || null);

      const rawMin = params.get('precioMin');
      const rawMax = params.get('precioMax');
      this.precioMin.set(rawMin !== null && rawMin !== '' && !Number.isNaN(Number(rawMin)) ? Number(rawMin) : null);
      this.precioMax.set(rawMax !== null && rawMax !== '' && !Number.isNaN(Number(rawMax)) ? Number(rawMax) : null);

      this.cargar(0);
    });
  }

  cargar(page: number): void {
    this.prods.cargarPagina(
      page,
      false,
      this.busqueda(),
      this.categoria(),
      false,
      this.disponibilidad(),
      this.precioMin(),
      this.precioMax()
    );
  }

  buscar(e: Event): void {
    this.busqueda.set((e.target as HTMLInputElement).value);
  }

  actualizarPrecioMin(e: Event): void {
    const val = (e.target as HTMLInputElement).value.trim();
    const num = Number(val);
    this.precioMin.set(val === '' || Number.isNaN(num) || num < 0 ? null : num);
  }

  actualizarPrecioMax(e: Event): void {
    const val = (e.target as HTMLInputElement).value.trim();
    const num = Number(val);
    this.precioMax.set(val === '' || Number.isNaN(num) || num < 0 ? null : num);
  }

  aplicar(): void {
    this.router.navigate(['/productos'], {
      queryParams: {
        q: this.busqueda().trim() || null,
        categoria: this.categoria(),
        precioMin: this.precioMin(),
        precioMax: this.precioMax()
      }
    });
  }

  filtrar(id: number | null): void {
    this.categoria.set(id);
    this.aplicar();
  }

  disponibilidadCambio(e: Event): void {
    this.disponibilidad.set((e.target as HTMLSelectElement).value);
    this.cargar(0);
  }

  tieneFiltrosActivos(): boolean {
    return Boolean(
      this.busqueda().trim() ||
        this.categoria() !== null ||
        this.precioMin() !== null ||
        this.precioMax() !== null ||
        this.disponibilidad()
    );
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoria.set(null);
    this.precioMin.set(null);
    this.precioMax.set(null);
    this.disponibilidad.set('');
    this.router.navigate(['/productos']);
  }

  estado(p: Producto): string {
    return p.cantidad === 0 ? 'Agotado' : p.cantidad <= p.stockMinimo ? 'Stock bajo' : 'Disponible';
  }

  imagen(url: string): string {
    return API_BASE.replace('/api/v1', '') + url;
  }

  abrirDetalle(p: Producto, e: Event): void {
    const target = e.target as HTMLElement;
    if (target.closest('button,a,input,select')) return;
    this.router.navigate(['/producto', p.id]);
  }

  agregar(p: Producto): void {
    if (!this.auth.estaAutenticado()) {
      this.router.navigate(['/login']);
      return;
    }
    this.ocupados.update((ids) => [...ids, p.id]);
    this.carrito.agregarItem(p.id, 1).subscribe({
      next: () => {
        this.mensaje.set('Producto añadido al carrito.');
        this.ocupados.update((ids) => ids.filter((id) => id !== p.id));
      },
      error: (e) => {
        this.mensaje.set(this.auth.mensajeError(e));
        this.ocupados.update((ids) => ids.filter((id) => id !== p.id));
      }
    });
  }
}