import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { ProductosService } from '../../services/productos.service';
import { AuthService } from '../../services/auth.service';
import { API_BASE } from '../../config/api.config';
import { Producto } from '../../models/producto.model';
import { DetalleReporte, ReporteVentas } from '../../models/carrito-pedido.model';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

interface DetalleEnriquecido extends DetalleReporte {
  categoriaId: number | null;
  categoriaNombre: string;
  precioUnitario: number;
}

interface ReporteFiltrado extends Omit<ReporteVentas, 'detalles'> {
  detalles: DetalleEnriquecido[];
  unidadesFiltradas: number;
  ingresosFiltrados: number;
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <div>
          <h2>📊 Reportes y Estadísticas de Ventas</h2>
          <p class="subtitle">Análisis de ingresos, modelos más vendidos y exportación en PDF / Excel</p>
        </div>

        <div class="header-actions">
          <div class="generate-box">
            <select [value]="mesGenerar()" (change)="mesGenerar.set(+$any($event.target).value)" aria-label="Mes del reporte">
              @for (m of meses; track m.valor) {
                <option [value]="m.valor">{{ m.nombre }}</option>
              }
            </select>
            <input
              type="number"
              min="2020"
              max="2035"
              [value]="anioGenerar()"
              (input)="anioGenerar.set(+$any($event.target).value)"
              aria-label="Año del reporte"
            />
            <button (click)="generarReporteMensual()" [disabled]="generando()" class="btn btn-primary">
              {{ generando() ? 'Calculando...' : '🔄 Generar Reporte' }}
            </button>
          </div>
        </div>
      </header>

      @if (mensajeExito()) {
        <div class="alert alert-success">{{ mensajeExito() }}</div>
      }
      @if (mensajeError()) {
        <div class="alert alert-error">{{ mensajeError() }}</div>
      }

      <!-- Barra de filtros y exportación -->
      <section class="filters-card" aria-label="Filtros de reportes">
        <div class="filters-grid">
          <div class="filter-field">
            <label for="rep-desde">Fecha desde</label>
            <input
              id="rep-desde"
              type="date"
              [value]="fechaDesde()"
              (change)="fechaDesde.set($any($event.target).value)"
            />
          </div>

          <div class="filter-field">
            <label for="rep-hasta">Fecha hasta</label>
            <input
              id="rep-hasta"
              type="date"
              [value]="fechaHasta()"
              (change)="fechaHasta.set($any($event.target).value)"
            />
          </div>

          <div class="filter-field">
            <label for="rep-modelo">Modelo o SKU</label>
            <input
              id="rep-modelo"
              type="search"
              placeholder="Ej. Urbana, MOCH001..."
              [value]="filtroModelo()"
              (input)="filtroModelo.set($any($event.target).value)"
            />
          </div>

          <div class="filter-field">
            <label for="rep-categoria">Categoría</label>
            <select
              id="rep-categoria"
              [value]="filtroCategoria() ?? ''"
              (change)="cambiarCategoria($event)"
            >
              <option value="">Todas las categorías</option>
              @for (cat of productosService.categorias(); track cat.id) {
                <option [value]="cat.id">{{ cat.nombre }}</option>
              }
            </select>
          </div>

          <div class="filter-field">
            <label for="rep-orden">Orden / Más vendido</label>
            <select
              id="rep-orden"
              [value]="ordenFiltro()"
              (change)="ordenFiltro.set($any($event.target).value)"
            >
              <option value="default">Orden registrado</option>
              <option value="mas_vendido">Más vendidos (mayor cantidad)</option>
              <option value="mayor_ingreso">Mayores ingresos (S/)</option>
              <option value="solo_top">Solo el más vendido (Top 1)</option>
            </select>
          </div>
        </div>

        <div class="filters-footer">
          <div class="filter-status">
            <span>Mostrando <strong>{{ reportesFiltrados().length }}</strong> reporte(s)</span>
            @if (hayFiltrosActivos()) {
              <button type="button" class="btn-link" (click)="limpiarFiltros()">Limpiar filtros</button>
            }
          </div>

          <div class="export-buttons">
            <button
              type="button"
              class="btn btn-excel"
              [disabled]="reportesFiltrados().length === 0"
              (click)="exportarExcel()"
            >
              📗 Exportar Excel
            </button>
            <button
              type="button"
              class="btn btn-pdf"
              [disabled]="reportesFiltrados().length === 0"
              (click)="exportarPdf()"
            >
              📕 Exportar PDF
            </button>
          </div>
        </div>
      </section>

      <!-- Resumen general según los filtros -->
      @if (!cargando() && reportesFiltrados().length > 0) {
        <section class="summary-grid" aria-label="Resumen general">
          <div class="summary-box">
            <span class="summary-label">Reportes visibles</span>
            <strong class="summary-value">{{ reportesFiltrados().length }}</strong>
          </div>
          <div class="summary-box">
            <span class="summary-label">Unidades vendidas</span>
            <strong class="summary-value">{{ resumenGeneral().unidades }}</strong>
          </div>
          <div class="summary-box">
            <span class="summary-label">Ingresos acumulados</span>
            <strong class="summary-value success">S/ {{ resumenGeneral().ingresos | number:'1.2-2' }}</strong>
          </div>
          <div class="summary-box highlight">
            <span class="summary-label">🏆 Modelo más vendido</span>
            <strong class="summary-value top-model">{{ resumenGeneral().masVendido }}</strong>
          </div>
        </section>
      }

      @if (cargando()) {
        <div class="loading">Cargando reportes...</div>
      } @else if (reportesService.reportes().length === 0) {
        <div class="empty-state">
          No hay reportes de ventas generados. Selecciona el mes/año y haz clic en "Generar Reporte".
        </div>
      } @else if (reportesFiltrados().length === 0) {
        <div class="empty-state">
          No se encontraron reportes o modelos con los filtros seleccionados.
          <br />
          <button type="button" class="btn btn-secondary" style="margin-top: 0.75rem" (click)="limpiarFiltros()">
            Restablecer filtros
          </button>
        </div>
      } @else {
        <div class="reportes-grid">
          @for (rep of reportesFiltrados(); track rep.id) {
            <div class="reporte-card">
              <div class="reporte-header">
                <div>
                  <h3>Reporte #{{ rep.id }}</h3>
                  <span class="reporte-periodo">
                    Período: {{ rep.fechaInicio | date:'dd/MM/yyyy' }} al {{ rep.fechaFin | date:'dd/MM/yyyy' }}
                  </span>
                </div>
                <div class="kpis">
                  <div class="kpi-item">
                    <span class="kpi-label">Mochilas Vendidas</span>
                    <span class="kpi-val">{{ rep.unidadesFiltradas }}</span>
                  </div>
                  <div class="kpi-item">
                    <span class="kpi-label">Ingresos Totales</span>
                    <span class="kpi-val success">S/ {{ rep.ingresosFiltrados | number:'1.2-2' }}</span>
                  </div>
                </div>
              </div>

              <div class="reporte-details">
                <h4>Desglose por Mochila:</h4>
                @if (rep.detalles.length > 0) {
                  <div class="table-wrapper">
                    <table class="subtable">
                      <thead>
                        <tr>
                          <th>#</th>
                          <th>SKU</th>
                          <th>Modelo</th>
                          <th>Categoría</th>
                          <th>Precio Unit.</th>
                          <th>Cant. Vendida</th>
                          <th>Ingresos Generados</th>
                          <th>Participación</th>
                        </tr>
                      </thead>
                      <tbody>
                        @for (det of rep.detalles; track det.id; let idx = $index) {
                          <tr [class.top-row]="idx === 0 && (ordenFiltro() === 'mas_vendido' || ordenFiltro() === 'solo_top')">
                            <td>
                              @if (idx === 0 && (ordenFiltro() === 'mas_vendido' || ordenFiltro() === 'solo_top')) {
                                <span class="badge-top">🏆 Top 1</span>
                              } @else {
                                {{ idx + 1 }}
                              }
                            </td>
                            <td><code>{{ det.productoCodigo }}</code></td>
                            <td><strong>{{ det.productoModelo }}</strong></td>
                            <td><span class="badge-cat">{{ det.categoriaNombre }}</span></td>
                            <td>S/ {{ det.precioUnitario | number:'1.2-2' }}</td>
                            <td><strong>{{ det.cantidadVendida }}</strong></td>
                            <td>S/ {{ det.ingresos | number:'1.2-2' }}</td>
                            <td>
                              {{ rep.ingresosFiltrados > 0 ? ((det.ingresos / rep.ingresosFiltrados) * 100 | number:'1.1-1') : '0.0' }}%
                            </td>
                          </tr>
                        }
                      </tbody>
                    </table>
                  </div>
                } @else {
                  <p class="no-details">No hubo ventas registradas en este período.</p>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem 0; }
    .page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem; }
    .page-header h2 { margin: 0; color: #1e293b; }
    .subtitle { color: #64748b; font-size: 0.9rem; margin-top: 0.25rem; }
    .header-actions { display: flex; align-items: center; gap: 0.75rem; }
    .generate-box { display: flex; align-items: center; gap: 0.5rem; background: #f8fafc; padding: 0.4rem; border: 1px solid #e2e8f0; border-radius: 10px; }
    .generate-box select, .generate-box input { padding: 0.45rem 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem; background: white; }
    .generate-box input { width: 82px; }

    .filters-card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; margin-bottom: 1.5rem; box-shadow: 0 2px 4px rgba(0,0,0,0.03); }
    .filters-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 0.9rem; }
    .filter-field { display: flex; flex-direction: column; gap: 0.35rem; }
    .filter-field label { font-size: 0.75rem; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.03em; }
    .filter-field input, .filter-field select { padding: 0.55rem 0.7rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.85rem; color: #1e293b; background: #fff; }
    .filter-field input:focus, .filter-field select:focus { outline: none; border-color: #2563eb; }
    .filters-footer { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem; margin-top: 1rem; padding-top: 0.9rem; border-top: 1px solid #f1f5f9; }
    .filter-status { display: flex; align-items: center; gap: 0.9rem; font-size: 0.85rem; color: #64748b; }
    .btn-link { background: none; border: none; color: #2563eb; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: underline; padding: 0; }
    .export-buttons { display: flex; gap: 0.6rem; }

    .summary-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .summary-box { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1rem 1.2rem; }
    .summary-box.highlight { background: #fffbeb; border-color: #fde68a; }
    .summary-label { display: block; font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 0.3rem; }
    .summary-value { font-size: 1.35rem; font-weight: 800; color: #1e293b; }
    .summary-value.success { color: #16a34a; }
    .summary-value.top-model { font-size: 1.05rem; color: #92400e; }

    .reportes-grid { display: flex; flex-direction: column; gap: 1.5rem; }
    .reporte-card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.5rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
    .reporte-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 1rem; margin-bottom: 1rem; }
    .reporte-header h3 { margin: 0; color: #1e293b; }
    .reporte-periodo { color: #64748b; font-size: 0.85rem; }
    .kpis { display: flex; gap: 2rem; }
    .kpi-item { text-align: right; }
    .kpi-label { display: block; font-size: 0.75rem; color: #64748b; font-weight: 600; text-transform: uppercase; }
    .kpi-val { font-size: 1.35rem; font-weight: 800; color: #1e293b; }
    .kpi-val.success { color: #16a34a; }

    .table-wrapper { overflow-x: auto; }
    .subtable { width: 100%; border-collapse: collapse; margin-top: 0.5rem; font-size: 0.88rem; }
    .subtable th, .subtable td { padding: 0.65rem 0.75rem; text-align: left; border-bottom: 1px solid #f1f5f9; }
    .subtable th { background: #f8fafc; color: #475569; font-weight: 600; white-space: nowrap; }
    .top-row { background: #fffbeb; }
    .badge-top { display: inline-block; background: #fef3c7; color: #92400e; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 999px; }
    .badge-cat { display: inline-block; background: #f1f5f9; color: #334155; font-size: 0.78rem; font-weight: 600; padding: 0.2rem 0.55rem; border-radius: 999px; }
    .reporte-details h4 { margin: 0; font-size: 0.95rem; color: #334155; }
    .no-details { color: #64748b; font-size: 0.88rem; margin-top: 0.5rem; }
    .empty-state, .loading { text-align: center; padding: 3rem; background: white; border-radius: 8px; color: #64748b; border: 1px dashed #cbd5e1; }

    .btn { padding: 0.55rem 0.95rem; border-radius: 8px; border: none; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: opacity 0.15s; }
    .btn:disabled { opacity: 0.45; cursor: not-allowed; }
    .btn-primary { background: #2563eb; color: white; }
    .btn-secondary { background: #e2e8f0; color: #1e293b; }
    .btn-excel { background: #15803d; color: white; }
    .btn-pdf { background: #dc2626; color: white; }
    .alert { padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.5rem; }
    .alert-success { background: #dcfce7; color: #166534; }
    .alert-error { background: #fee2e2; color: #991b1b; }

    @media (max-width: 1024px) {
      .filters-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      .summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    }
    @media (max-width: 600px) {
      .filters-grid, .summary-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class ReportesPage implements OnInit {
  protected readonly reportesService = inject(CarritoPedidoService);
  protected readonly productosService = inject(ProductosService);
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);

  readonly cargando = signal(false);
  readonly generando = signal(false);
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');

  readonly mesGenerar = signal(11);
  readonly anioGenerar = signal(2026);
  readonly meses = [
    { valor: 1, nombre: 'Enero' },
    { valor: 2, nombre: 'Febrero' },
    { valor: 3, nombre: 'Marzo' },
    { valor: 4, nombre: 'Abril' },
    { valor: 5, nombre: 'Mayo' },
    { valor: 6, nombre: 'Junio' },
    { valor: 7, nombre: 'Julio' },
    { valor: 8, nombre: 'Agosto' },
    { valor: 9, nombre: 'Septiembre' },
    { valor: 10, nombre: 'Octubre' },
    { valor: 11, nombre: 'Noviembre' },
    { valor: 12, nombre: 'Diciembre' }
  ];

  // Mapa de productos para cruzar categoría y precio unitario con cada detalle
  private readonly productosPorId = signal(new Map<number, Producto>());

  // Filtros combinables
  readonly fechaDesde = signal('');
  readonly fechaHasta = signal('');
  readonly filtroModelo = signal('');
  readonly filtroCategoria = signal<number | null>(null);
  readonly ordenFiltro = signal<'default' | 'mas_vendido' | 'mayor_ingreso' | 'solo_top'>('default');

  readonly hayFiltrosActivos = computed(() =>
    Boolean(
      this.fechaDesde() ||
        this.fechaHasta() ||
        this.filtroModelo().trim() ||
        this.filtroCategoria() !== null ||
        this.ordenFiltro() !== 'default'
    )
  );

  readonly reportesFiltrados = computed<ReporteFiltrado[]>(() => {
    const desde = this.fechaDesde();
    const hasta = this.fechaHasta();
    const modeloQuery = this.filtroModelo().trim().toLowerCase();
    const catId = this.filtroCategoria();
    const orden = this.ordenFiltro();
    const mapaProductos = this.productosPorId();
    const filtraDetalle = Boolean(modeloQuery || catId !== null);

    return this.reportesService
      .reportes()
      .filter((rep) => {
        const repInicio = rep.fechaInicio ? rep.fechaInicio.slice(0, 10) : '';
        const repFin = rep.fechaFin ? rep.fechaFin.slice(0, 10) : '';
        if (desde && repFin && repFin < desde) return false;
        if (hasta && repInicio && repInicio > hasta) return false;
        return true;
      })
      .map((rep) => {
        let detalles: DetalleEnriquecido[] = (rep.detalles ?? []).map((det) => {
          const prod = mapaProductos.get(det.productoId);
          return {
            ...det,
            categoriaId: prod?.categoriaId ?? null,
            categoriaNombre: prod?.categoriaNombre ?? 'Sin categoría',
            precioUnitario: prod
              ? Number(prod.precio)
              : det.cantidadVendida > 0
                ? Number(det.ingresos) / det.cantidadVendida
                : 0
          };
        });

        if (modeloQuery) {
          detalles = detalles.filter(
            (d) =>
              d.productoModelo.toLowerCase().includes(modeloQuery) ||
              d.productoCodigo.toLowerCase().includes(modeloQuery)
          );
        }

        if (catId !== null) {
          detalles = detalles.filter((d) => d.categoriaId === catId);
        }

        if (orden === 'mas_vendido' || orden === 'solo_top') {
          detalles = [...detalles].sort(
            (a, b) => b.cantidadVendida - a.cantidadVendida || Number(b.ingresos) - Number(a.ingresos)
          );
        } else if (orden === 'mayor_ingreso') {
          detalles = [...detalles].sort((a, b) => Number(b.ingresos) - Number(a.ingresos));
        }

        if (orden === 'solo_top' && detalles.length > 0) {
          detalles = [detalles[0]];
        }

        const unidadesFiltradas =
          filtraDetalle || orden === 'solo_top'
            ? detalles.reduce((acc, d) => acc + d.cantidadVendida, 0)
            : rep.totalProductosVendidos;

        const ingresosFiltrados =
          filtraDetalle || orden === 'solo_top'
            ? detalles.reduce((acc, d) => acc + Number(d.ingresos), 0)
            : Number(rep.totalIngresos);

        return {
          ...rep,
          detalles,
          unidadesFiltradas,
          ingresosFiltrados
        };
      })
      .filter((rep) => !filtraDetalle || rep.detalles.length > 0);
  });

  readonly resumenGeneral = computed(() => {
    const lista = this.reportesFiltrados();
    let unidades = 0;
    let ingresos = 0;
    const acumuladoPorModelo = new Map<string, { modelo: string; cantidad: number }>();

    for (const rep of lista) {
      unidades += rep.unidadesFiltradas;
      ingresos += rep.ingresosFiltrados;
      for (const det of rep.detalles) {
        const key = `${det.productoCodigo} - ${det.productoModelo}`;
        const actual = acumuladoPorModelo.get(key) ?? { modelo: key, cantidad: 0 };
        actual.cantidad += det.cantidadVendida;
        acumuladoPorModelo.set(key, actual);
      }
    }

    let top = 'Sin ventas';
    let maxCant = 0;
    for (const item of acumuladoPorModelo.values()) {
      if (item.cantidad > maxCant) {
        maxCant = item.cantidad;
        top = `${item.modelo} (${item.cantidad} und.)`;
      }
    }

    return { unidades, ingresos, masVendido: top };
  });

  ngOnInit(): void {
    this.cargando.set(true);
    this.productosService.cargarCategorias();

    this.http.get<Producto[]>(`${API_BASE}/productos`).subscribe({
      next: (lista) => {
        const mapa = new Map<number, Producto>();
        for (const p of lista) mapa.set(p.id, p);
        this.productosPorId.set(mapa);
      }
    });

    this.reportesService.listarReportes().subscribe({
      next: () => this.cargando.set(false),
      error: () => this.cargando.set(false)
    });
  }

  cambiarCategoria(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.filtroCategoria.set(val === '' ? null : Number(val));
  }

  limpiarFiltros(): void {
    this.fechaDesde.set('');
    this.fechaHasta.set('');
    this.filtroModelo.set('');
    this.filtroCategoria.set(null);
    this.ordenFiltro.set('default');
  }

  generarReporteMensual(): void {
    this.generando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');

    this.reportesService.generarReporteMensual(this.mesGenerar(), this.anioGenerar()).subscribe({
      next: (rep) => {
        this.generando.set(false);
        this.mensajeExito.set(`Reporte mensual #${rep.id} generado exitosamente.`);
        setTimeout(() => this.mensajeExito.set(''), 4000);
      },
      error: (err) => {
        this.generando.set(false);
        this.mensajeError.set(this.auth.mensajeError(err));
      }
    });
  }

  exportarExcel(): void {
    const reportes = this.reportesFiltrados();
    if (reportes.length === 0) return;

    const filasDetalle: Record<string, string | number>[] = [];
    const filasResumen: Record<string, string | number>[] = [];

    for (const rep of reportes) {
      const periodo = `${this.formatearFecha(rep.fechaInicio)} - ${this.formatearFecha(rep.fechaFin)}`;
      filasResumen.push({
        'Reporte #': rep.id,
        'Período': periodo,
        'Unidades Vendidas': rep.unidadesFiltradas,
        'Ingresos Totales (S/)': Number(rep.ingresosFiltrados.toFixed(2))
      });

      for (const det of rep.detalles) {
        const participacion =
          rep.ingresosFiltrados > 0
            ? Number(((Number(det.ingresos) / rep.ingresosFiltrados) * 100).toFixed(1))
            : 0;
        filasDetalle.push({
          'Reporte #': rep.id,
          'Período': periodo,
          'SKU': det.productoCodigo,
          'Modelo': det.productoModelo,
          'Categoría': det.categoriaNombre,
          'Precio Unitario (S/)': Number(det.precioUnitario.toFixed(2)),
          'Cantidad Vendida': det.cantidadVendida,
          'Ingresos Generados (S/)': Number(Number(det.ingresos).toFixed(2)),
          'Participación (%)': participacion
        });
      }
    }

    const wb = XLSX.utils.book_new();
    const wsDetalle = XLSX.utils.json_to_sheet(
      filasDetalle.length > 0 ? filasDetalle : [{ Mensaje: 'Sin detalles en los reportes seleccionados' }]
    );
    const wsResumen = XLSX.utils.json_to_sheet(filasResumen);

    XLSX.utils.book_append_sheet(wb, wsDetalle, 'Detalle Ventas');
    XLSX.utils.book_append_sheet(wb, wsResumen, 'Resumen Reportes');

    const fechaArchivo = new Date().toISOString().slice(0, 10);
    XLSX.writeFile(wb, `MHM_Store_Reportes_${fechaArchivo}.xlsx`);
  }

  exportarPdf(): void {
    const reportes = this.reportesFiltrados();
    if (reportes.length === 0) return;

    const doc = new jsPDF({ orientation: 'landscape' });
    const resumen = this.resumenGeneral();

    doc.setFontSize(16);
    doc.text('MHM Store - Reporte de Ventas', 14, 16);

    doc.setFontSize(9);
    doc.setTextColor(100);
    doc.text(`Generado el: ${new Date().toLocaleString('es-PE')}`, 14, 22);
    doc.text(
      `Total Reportes: ${reportes.length}  |  Unidades Vendidas: ${resumen.unidades}  |  Ingresos Totales: S/ ${resumen.ingresos.toFixed(2)}  |  Más Vendido: ${resumen.masVendido}`,
      14,
      28
    );

    const filasTabla: (string | number)[][] = [];
    for (const rep of reportes) {
      const periodo = `${this.formatearFecha(rep.fechaInicio)} al ${this.formatearFecha(rep.fechaFin)}`;
      if (rep.detalles.length === 0) {
        filasTabla.push([`#${rep.id}`, periodo, '-', 'Sin ventas registradas', '-', '-', 0, 'S/ 0.00']);
      } else {
        for (const det of rep.detalles) {
          filasTabla.push([
            `#${rep.id}`,
            periodo,
            det.productoCodigo,
            det.productoModelo,
            det.categoriaNombre,
            `S/ ${det.precioUnitario.toFixed(2)}`,
            det.cantidadVendida,
            `S/ ${Number(det.ingresos).toFixed(2)}`
          ]);
        }
      }
    }

    autoTable(doc, {
      startY: 34,
      head: [['Reporte', 'Período', 'SKU', 'Modelo', 'Categoría', 'Precio Unit.', 'Cant.', 'Ingresos']],
      body: filasTabla,
      styles: { fontSize: 8.5, cellPadding: 2.5 },
      headStyles: { fillColor: [30, 41, 59], textColor: 255, fontStyle: 'bold' },
      alternateRowStyles: { fillColor: [248, 250, 252] }
    });

    const fechaArchivo = new Date().toISOString().slice(0, 10);
    doc.save(`MHM_Store_Reportes_${fechaArchivo}.pdf`);
  }

  private formatearFecha(iso: string): string {
    if (!iso) return '';
    const [anio, mes, dia] = iso.slice(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }
}