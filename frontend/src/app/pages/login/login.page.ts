import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="brand">
          <h2>🎒 MHM Store</h2>
          <p class="subtitle">Sistema Integrado de Gestión de Mochilas</p>
        </div>

        <div class="tabs">
          <button type="button" class="tab-btn" [class.active]="modo() === 'login'" (click)="cambiarModo('login')">Iniciar Sesión</button>
          <button type="button" class="tab-btn" [class.active]="modo() === 'registro'" (click)="cambiarModo('registro')">Crear cuenta cliente</button>
        </div>

        @if (errorMsg()) {
          <div class="alert alert-error">
            {{ errorMsg() }}
          </div>
        }

        @if (exitoMsg()) {
          <div class="alert alert-success">
            {{ exitoMsg() }}
          </div>
        }

        <!-- FORMULARIO DE INICIO DE SESIÓN -->
        @if (modo() === 'login') {
          <form [formGroup]="formLogin" (ngSubmit)="onLogin()">
            <div class="form-group">
              <label for="login-email">Correo Electrónico</label>
              <input
                id="login-email"
                type="email"
                formControlName="email"
                placeholder="ejemplo@mhmstore.com"
                [class.invalid]="formLogin.get('email')?.invalid && formLogin.get('email')?.touched"
              />
              @if (formLogin.get('email')?.invalid && formLogin.get('email')?.touched) {
                <small class="error-text">Ingrese un correo válido.</small>
              }
            </div>

            <div class="form-group">
              <label for="login-contrasena">Contraseña</label>
              <input
                id="login-contrasena"
                type="password"
                formControlName="contrasena"
                placeholder="••••••••"
                [class.invalid]="formLogin.get('contrasena')?.invalid && formLogin.get('contrasena')?.touched"
              />
              @if (formLogin.get('contrasena')?.invalid && formLogin.get('contrasena')?.touched) {
                <small class="error-text">La contraseña es obligatoria (mínimo 3 caracteres).</small>
              }
            </div>

            <button type="submit" [disabled]="formLogin.invalid || cargando()" class="btn btn-primary">
              @if (cargando()) {
                <span>Verificando...</span>
              } @else {
                <span>Ingresar</span>
              }
            </button>
          </form>

          <div class="demo-hints"><p>¿Primera vez en MHM Store? Crea una cuenta de cliente para comprar y seguir tus pedidos.</p></div>
        }

        <!-- FORMULARIO DE REGISTRO DE NUEVO USUARIO -->
        @if (modo() === 'registro') {
          <form [formGroup]="formRegistro" (ngSubmit)="onRegistro()">
            <div class="form-group">
              <label for="reg-nombre">Nombre Completo *</label>
              <input
                id="reg-nombre"
                type="text"
                formControlName="nombre"
                placeholder="Ej. Roberto Gómez"
                [class.invalid]="formRegistro.get('nombre')?.invalid && formRegistro.get('nombre')?.touched"
              />
              @if (formRegistro.get('nombre')?.invalid && formRegistro.get('nombre')?.touched) {
                <small class="error-text">El nombre es obligatorio.</small>
              }
            </div>

            <div class="form-group">
              <label for="reg-email">Correo Electrónico *</label>
              <input
                id="reg-email"
                type="email"
                formControlName="email"
                placeholder="usuario@mhmstore.com"
                [class.invalid]="formRegistro.get('email')?.invalid && formRegistro.get('email')?.touched"
              />
              @if (formRegistro.get('email')?.invalid && formRegistro.get('email')?.touched) {
                <small class="error-text">Ingrese un correo válido.</small>
              }
            </div>

            <div class="form-group">
              <label for="reg-contrasena">Contraseña *</label>
              <input
                id="reg-contrasena"
                type="password"
                formControlName="contrasena"
                placeholder="Mínimo 6 caracteres"
                [class.invalid]="formRegistro.get('contrasena')?.invalid && formRegistro.get('contrasena')?.touched"
              />
              @if (formRegistro.get('contrasena')?.invalid && formRegistro.get('contrasena')?.touched) {
                <small class="error-text">La contraseña debe tener al menos 6 caracteres.</small>
              }
            </div>

            <button type="submit" [disabled]="formRegistro.invalid || cargando()" class="btn btn-success">
              @if (cargando()) {
                <span>Creando cuenta...</span>
              } @else {
                <span>Registrarme e Ingresar</span>
              }
            </button>
          </form>
        }
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 80vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
    }
    .login-card {
      background: white;
      border-radius: 22px;
      padding: 2.25rem;
      width: 100%;
      max-width: 440px;
      box-shadow: 0 12px 36px rgba(0,0,0,0.055);
      border: 1px solid #e7e7e7;
    }
    .brand {
      text-align: center;
      margin-bottom: 1.5rem;
    }
    .brand h2 {
      margin: 0;
      color: #1e293b;
      font-size: 1.75rem;
    }
    .subtitle {
      color: #64748b;
      font-size: 0.9rem;
      margin-top: 0.25rem;
    }
    .tabs {
      display: flex;
      background: #f1f5f9;
      padding: 0.25rem;
      border-radius: 8px;
      margin-bottom: 1.5rem;
    }
    .tab-btn {
      flex: 1;
      padding: 0.55rem;
      border: none;
      background: transparent;
      border-radius: 6px;
      font-weight: 600;
      font-size: 0.9rem;
      color: #64748b;
      cursor: pointer;
      transition: all 0.2s;
    }
    .tab-btn.active {
      background: white;
      color: #050505;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
    .form-group {
      margin-bottom: 1.15rem;
    }
    label {
      display: block;
      margin-bottom: 0.4rem;
      font-weight: 600;
      font-size: 0.85rem;
      color: #334155;
    }
    input, select {
      width: 100%;
      padding: 0.7rem 0.9rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      transition: all 0.2s;
      box-sizing: border-box;
    }
    input:focus, select:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }
    input.invalid {
      border-color: #ef4444;
      background-color: #fef2f2;
    }
    .error-text {
      color: #ef4444;
      font-size: 0.8rem;
      margin-top: 0.25rem;
      display: block;
    }
    .btn {
      width: 100%;
      padding: 0.75rem;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }
    .btn-primary {
      background: #050505;
      color: white;
    }
    .btn-primary:hover:not(:disabled) {
      background: #333;
    }
    .btn-success {
      background: #050505;
      color: white;
    }
    .btn-success:hover:not(:disabled) {
      background: #333;
    }
    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .alert {
      padding: 0.75rem 1rem;
      border-radius: 8px;
      font-size: 0.875rem;
      margin-bottom: 1.25rem;
    }
    .alert-error {
      background: #fee2e2;
      color: #991b1b;
      border: 1px solid #fecaca;
    }
    .alert-success {
      background: #dcfce7;
      color: #166534;
      border: 1px solid #bbf7d0;
    }
    .demo-hints {
      margin-top: 1.5rem;
      padding-top: 1.25rem;
      border-top: 1px dashed #e2e8f0;
      font-size: 0.85rem;
      color: #64748b;
    }
    .credentials-badge {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      padding: 0.5rem 0.75rem;
      border-radius: 6px;
      margin-top: 0.4rem;
      cursor: pointer;
      font-size: 0.825rem;
      transition: background 0.15s;
    }
    .credentials-badge:hover {
      background: #e2e8f0;
    }
    code {
      background: #e2e8f0;
      padding: 0.1rem 0.3rem;
      border-radius: 4px;
    }
  `]
})
export class LoginPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly modo = signal<'login' | 'registro'>('login');
  readonly cargando = signal(false);
  readonly errorMsg = signal('');
  readonly exitoMsg = signal('');

  readonly formLogin = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly formRegistro = this.fb.group({
    nombre: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('registro') === 'cliente') this.modo.set('registro');
  }

  cambiarModo(nuevoModo: 'login' | 'registro'): void {
    this.modo.set(nuevoModo);
    this.errorMsg.set('');
    this.exitoMsg.set('');
  }

  cargarCredenciales(email: string, contrasena: string): void {
    this.formLogin.patchValue({ email, contrasena });
    this.errorMsg.set('');
    this.exitoMsg.set('');
  }

  onLogin(): void {
    if (this.formLogin.invalid) return;

    this.cargando.set(true);
    this.errorMsg.set('');

    const creds = {
      email: this.formLogin.value.email!,
      contrasena: this.formLogin.value.contrasena!
    };

    this.auth.login(creds).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate(['/productos']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMsg.set(this.auth.mensajeError(err));
      }
    });
  }

  onRegistro(): void {
    if (this.formRegistro.invalid) return;

    this.cargando.set(true);
    this.errorMsg.set('');
    this.exitoMsg.set('');

    const req = {
      nombre: this.formRegistro.value.nombre!,
      email: this.formRegistro.value.email!,
      contrasena: this.formRegistro.value.contrasena!
    };

    this.auth.registrar(req).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate(['/productos']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMsg.set(this.auth.mensajeError(err));
      }
    });
  }
}
