import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <div>
          <h2>📊 Reportes y Estadísticas de Ventas</h2>
          <p class="subtitle">Análisis de ingresos y productos vendidos calculados mediante JPQL</p>
        </div>
        <button (click)="generarReporteMesActual()" [disabled]="generando()" class="btn btn-primary">
          {{ generando() ? 'Calculando...' : '🔄 Generar Reporte Mensual (Nov 2026)' }}
        </button>
      </header>

      @if (mensajeExito()) {
        <div class="alert alert-success">{{ mensajeExito() }}</div>
      }
      @if (mensajeError()) {
        <div class="alert alert-error">{{ mensajeError() }}</div>
      }

      @if (cargando()) {
        <div class="loading">Cargando reportes...</div>
      } @else if (reportesService.reportes().length === 0) {
        <div class="empty-state">
          No hay reportes de ventas generados. Haz clic en "Generar Reporte Mensual".
        </div>
      } @else {
        <div class="reportes-grid">
          @for (rep of reportesService.reportes(); track rep.id) {
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
                    <span class="kpi-val">{{ rep.totalProductosVendidos }}</span>
                  </div>
                  <div class="kpi-item">
                    <span class="kpi-label">Ingresos Totales</span>
                    <span class="kpi-val success">S/ {{ rep.totalIngresos | number:'1.2-2' }}</span>
                  </div>
                </div>
              </div>

              <div class="reporte-details">
                <h4>Desglose por Mochila (DetalleReporte):</h4>
                @if (rep.detalles && rep.detalles.length > 0) {
                  <table class="subtable">
                    <thead>
                      <tr>
                        <th>SKU</th>
                        <th>Modelo</th>
                        <th>Cant. Vendida</th>
                        <th>Ingresos Generados</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (det of rep.detalles; track det.id) {
                        <tr>
                          <td><code>{{ det.productoCodigo }}</code></td>
                          <td>{{ det.productoModelo }}</td>
                          <td><strong>{{ det.cantidadVendida }}</strong></td>
                          <td>S/ {{ det.ingresos | number:'1.2-2' }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
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
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    .page-header h2 { margin: 0; color: #1e293b; }
    .subtitle { color: #64748b; font-size: 0.9rem; margin-top: 0.25rem; }
    .reportes-grid { display: flex; flex-direction: column; gap: 1.5rem; }
    .reporte-card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.5rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
    .reporte-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 1rem; margin-bottom: 1rem; }
    .reporte-header h3 { margin: 0; color: #1e293b; }
    .reporte-periodo { color: #64748b; font-size: 0.85rem; }
    .kpis { display: flex; gap: 2rem; }
    .kpi-item { text-align: right; }
    .kpi-label { display: block; font-size: 0.75rem; color: #64748b; font-weight: 600; text-transform: uppercase; }
    .kpi-val { font-size: 1.35rem; font-weight: 800; color: #1e293b; }
    .kpi-val.success { color: #16a34a; }
    .subtable { width: 100%; border-collapse: collapse; margin-top: 0.5rem; font-size: 0.9rem; }
    .subtable th, .subtable td { padding: 0.6rem 0.75rem; text-align: left; border-bottom: 1px solid #f1f5f9; }
    .subtable th { background: #f8fafc; color: #475569; font-weight: 600; }
    .reporte-details h4 { margin: 0; font-size: 0.95rem; color: #334155; }
    .empty-state { text-align: center; padding: 3rem; background: white; border-radius: 8px; color: #64748b; border: 1px dashed #cbd5e1; }
    .btn { padding: 0.6rem 1rem; border-radius: 8px; border: none; font-weight: 600; cursor: pointer; }
    .btn-primary { background: #2563eb; color: white; }
    .alert { padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.5rem; }
    .alert-success { background: #dcfce7; color: #166534; }
    .alert-error { background: #fee2e2; color: #991b1b; }
  `]
})
export class ReportesPage implements OnInit {
  protected readonly reportesService = inject(CarritoPedidoService);
  private readonly auth = inject(AuthService);

  readonly cargando = signal(false);
  readonly generando = signal(false);
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');

  ngOnInit(): void {
    this.cargando.set(true);
    this.reportesService.listarReportes().subscribe({
      next: () => this.cargando.set(false),
      error: () => this.cargando.set(false)
    });
  }

  generarReporteMesActual(): void {
    this.generando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');

    this.reportesService.generarReporteMensual(11, 2026).subscribe({
      next: (rep) => {
        this.generando.set(false);
        this.mensajeExito.set(`Reporte mensual #${rep.id} generado exitosamente con JPQL.`);
        setTimeout(() => this.mensajeExito.set(''), 4000);
      },
      error: (err) => {
        this.generando.set(false);
        this.mensajeError.set(this.auth.mensajeError(err));
      }
    });
  }
}
