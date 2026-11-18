import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuariosService } from '../../services/usuarios.service';
import { AuthService } from '../../services/auth.service';
import { Permiso, Rol, Usuario, UsuarioCreateRequest } from '../../models/auth.model';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page-container">
      <header class="page-header">
        <div>
          <h2>👥 Administración de Usuarios y Permisos</h2>
          <p class="subtitle">Crear trabajadores y seleccionar qué módulos específicos pueden operar</p>
        </div>
        <button (click)="abrirModalCrear()" class="btn btn-primary">
          ➕ Nuevo Trabajador / Admin
        </button>
      </header>

      @if (mensajeExito()) {
        <div class="alert alert-success">{{ mensajeExito() }}</div>
      }
      @if (mensajeError()) {
        <div class="alert alert-error">{{ mensajeError() }}</div>
      }

      @if (usuariosService.cargando()) {
        <div class="loading">Cargando usuarios...</div>
      } @else {
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Rol</th>
              <th>Permisos Asignados</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (u of usuariosService.usuarios(); track u.id) {
              <tr>
                <td>{{ u.id }}</td>
                <td><strong>{{ u.nombre }}</strong></td>
                <td>{{ u.email }}</td>
                <td>
                  <span class="badge" [class.badge-admin]="u.rol === 'ADMIN'" [class.badge-worker]="u.rol === 'TRABAJADOR'">
                    {{ u.rol }}
                  </span>
                </td>
                <td>
                  @if (u.rol === 'ADMIN') {
                    <span class="permiso-pill admin-pill">Acceso Total a todos los campos</span>
                  } @else {
                    <div class="permisos-wrapper">
                      @for (p of u.permisos; track p) {
                        <span class="permiso-pill">{{ p }}</span>
                      }
                      @if (!u.permisos || u.permisos.length === 0) {
                        <small class="no-perms">Sin permisos especiales</small>
                      }
                    </div>
                  }
                </td>
                <td>
                  <div class="actions-cell">
                    @if (u.rol === 'TRABAJADOR') {
                      <button (click)="abrirModalPermisos(u)" class="btn-sm btn-outline-primary" title="Gestionar Permisos">
                        🔑 Permisos
                      </button>
                    }
                    <button (click)="eliminar(u)" class="btn-icon delete" title="Eliminar usuario">🗑️</button>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }

      <!-- Modal Crear Usuario -->
      @if (mostrarModalCrear()) {
        <div class="modal-backdrop">
          <div class="modal-dialog">
            <div class="modal-header">
              <h3>Registrar Nuevo Usuario</h3>
              <button (click)="cerrarModal()" class="close-btn">&times;</button>
            </div>

            <form [formGroup]="formCrear" (ngSubmit)="guardarNuevoUsuario()">
              <div class="form-group">
                <label>Nombre Completo *</label>
                <input formControlName="nombre" placeholder="Ej. Juan Pérez" />
              </div>

              <div class="form-group">
                <label>Correo Electrónico *</label>
                <input type="email" formControlName="email" placeholder="juan@mhmstore.com" />
              </div>

              <div class="form-group">
                <label>Contraseña * (mínimo 6 caracteres)</label>
                <input type="password" formControlName="contrasena" placeholder="••••••••" />
              </div>

              <div class="form-group">
                <label>Rol de Usuario *</label>
                <select formControlName="rol">
                  <option value="TRABAJADOR">TRABAJADOR</option>
                  <option value="ADMIN">ADMIN (Acceso Total)</option>
                </select>
              </div>

              @if (formCrear.get('rol')?.value === 'TRABAJADOR') {
                <div class="form-group">
                  <label>Seleccionar Permisos del Trabajador:</label>
                  <div class="checkbox-grid">
                    @for (permiso of permisosDisponibles; track permiso) {
                      <label class="checkbox-label">
                        <input
                          type="checkbox"
                          [checked]="permisosSeleccionados().includes(permiso)"
                          (change)="togglePermiso(permiso)"
                        />
                        <span>{{ permiso }}</span>
                      </label>
                    }
                  </div>
                </div>
              }

              <div class="modal-footer">
                <button type="button" (click)="cerrarModal()" class="btn btn-secondary">Cancelar</button>
                <button type="submit" [disabled]="formCrear.invalid || guardando()" class="btn btn-primary">
                  {{ guardando() ? 'Guardando...' : 'Crear Usuario' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }

      <!-- Modal Editar Permisos -->
      @if (mostrarModalPermisos()) {
        <div class="modal-backdrop">
          <div class="modal-dialog">
            <div class="modal-header">
              <h3>Modificar Permisos de {{ usuarioSeleccionado()?.nombre }}</h3>
              <button (click)="cerrarModal()" class="close-btn">&times;</button>
            </div>

            <div class="form-group">
              <p class="subtitle">Activa o desactiva las capacidades del trabajador:</p>
              <div class="checkbox-grid">
                @for (permiso of permisosDisponibles; track permiso) {
                  <label class="checkbox-label">
                    <input
                      type="checkbox"
                      [checked]="permisosSeleccionados().includes(permiso)"
                      (change)="togglePermiso(permiso)"
                    />
                    <span>{{ permiso }}</span>
                  </label>
                }
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" (click)="cerrarModal()" class="btn btn-secondary">Cancelar</button>
              <button (click)="guardarPermisosActualizados()" [disabled]="guardando()" class="btn btn-primary">
                {{ guardando() ? 'Actualizando...' : 'Guardar Permisos' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem 0; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    .page-header h2 { margin: 0; color: #1e293b; }
    .subtitle { color: #64748b; font-size: 0.9rem; margin-top: 0.25rem; }
    .table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }
    .table th, .table td { padding: 0.85rem 1rem; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 0.9rem; }
    .table th { background: #f8fafc; font-weight: 600; color: #334155; }
    .badge { padding: 0.25rem 0.5rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }
    .badge-admin { background: #fee2e2; color: #991b1b; }
    .badge-worker { background: #e0f2fe; color: #0369a1; }
    .permisos-wrapper { display: flex; flex-wrap: wrap; gap: 0.35rem; }
    .permiso-pill { background: #f1f5f9; color: #475569; font-size: 0.725rem; padding: 0.15rem 0.4rem; border-radius: 4px; font-weight: 600; border: 1px solid #e2e8f0; }
    .admin-pill { background: #fef3c7; color: #b45309; border-color: #fde68a; }
    .actions-cell { display: flex; gap: 0.5rem; align-items: center; }
    .checkbox-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-top: 0.5rem; }
    .checkbox-label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; color: #334155; cursor: pointer; }
    .modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-dialog { background: white; border-radius: 12px; width: 100%; max-width: 520px; padding: 1.5rem; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .modal-header h3 { margin: 0; }
    .close-btn { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #94a3b8; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.35rem; }
    .form-group input, .form-group select { width: 100%; padding: 0.6rem; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
    .btn { padding: 0.6rem 1rem; border-radius: 8px; border: none; font-weight: 600; cursor: pointer; }
    .btn-primary { background: #2563eb; color: white; }
    .btn-secondary { background: #64748b; color: white; }
    .btn-outline-primary { background: white; border: 1px solid #2563eb; color: #2563eb; border-radius: 6px; }
    .btn-sm { font-size: 0.75rem; padding: 0.3rem 0.5rem; }
    .btn-icon.delete { background: #fee2e2; border: 1px solid #fca5a5; border-radius: 6px; padding: 0.3rem 0.5rem; cursor: pointer; }
    .alert { padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1.5rem; }
    .alert-success { background: #dcfce7; color: #166534; }
    .alert-error { background: #fee2e2; color: #991b1b; }
  `]
})
export class UsuariosPage implements OnInit {
  protected readonly usuariosService = inject(UsuariosService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly mostrarModalCrear = signal(false);
  readonly mostrarModalPermisos = signal(false);
  readonly usuarioSeleccionado = signal<Usuario | null>(null);
  readonly permisosSeleccionados = signal<Permiso[]>([]);
  readonly guardando = signal(false);
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');

  readonly permisosDisponibles: Permiso[] = [
    'VER_CATALOGOS',
    'GESTIONAR_PRODUCTOS',
    'GESTIONAR_INVENTARIO',
    'GESTIONAR_CATALOGOS',
    'GESTIONAR_CARRITO',
    'GESTIONAR_PEDIDOS',
    'VER_REPORTES'
  ];

  readonly formCrear: FormGroup = this.fb.group({
    nombre: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(6)]],
    rol: ['TRABAJADOR', [Validators.required]]
  });

  ngOnInit(): void {
    this.usuariosService.cargarUsuarios();
  }

  abrirModalCrear(): void {
    this.formCrear.reset({ rol: 'TRABAJADOR' });
    this.permisosSeleccionados.set(['VER_CATALOGOS', 'GESTIONAR_CARRITO', 'GESTIONAR_PEDIDOS']);
    this.mostrarModalCrear.set(true);
  }

  abrirModalPermisos(u: Usuario): void {
    this.usuarioSeleccionado.set(u);
    this.permisosSeleccionados.set([...(u.permisos || [])]);
    this.mostrarModalPermisos.set(true);
  }

  cerrarModal(): void {
    this.mostrarModalCrear.set(false);
    this.mostrarModalPermisos.set(false);
    this.usuarioSeleccionado.set(null);
  }

  togglePermiso(p: Permiso): void {
    const actuales = this.permisosSeleccionados();
    if (actuales.includes(p)) {
      this.permisosSeleccionados.set(actuales.filter((item) => item !== p));
    } else {
      this.permisosSeleccionados.set([...actuales, p]);
    }
  }

  guardarNuevoUsuario(): void {
    if (this.formCrear.invalid) return;

    this.guardando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');

    const req: UsuarioCreateRequest = {
      nombre: this.formCrear.value.nombre,
      email: this.formCrear.value.email,
      contrasena: this.formCrear.value.contrasena,
      rol: this.formCrear.value.rol,
      permisos: this.formCrear.value.rol === 'ADMIN' ? undefined : this.permisosSeleccionados()
    };

    this.usuariosService.crear(req).subscribe({
      next: (creado) => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mensajeExito.set(`Usuario "${creado.nombre}" registrado exitosamente.`);
        setTimeout(() => this.mensajeExito.set(''), 3000);
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(this.auth.mensajeError(err));
      }
    });
  }

  guardarPermisosActualizados(): void {
    const u = this.usuarioSeleccionado();
    if (!u) return;

    this.guardando.set(true);
    this.usuariosService.actualizarPermisos(u.id, this.permisosSeleccionados()).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarModal();
        this.mensajeExito.set(`Permisos de "${u.nombre}" actualizados correctamente.`);
        setTimeout(() => this.mensajeExito.set(''), 3000);
      },
      error: (err) => {
        this.guardando.set(false);
        this.mensajeError.set(this.auth.mensajeError(err));
      }
    });
  }

  eliminar(u: Usuario): void {
    if (!confirm(`¿Eliminar al usuario ${u.nombre} (${u.email})?`)) return;

    this.usuariosService.eliminar(u.id).subscribe({
      next: () => {
        this.mensajeExito.set('Usuario eliminado.');
        setTimeout(() => this.mensajeExito.set(''), 3000);
      },
      error: (err) => this.mensajeError.set(this.auth.mensajeError(err))
    });
  }
}
