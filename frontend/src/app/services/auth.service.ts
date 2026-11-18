import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { AuthResponse, LoginRequest, Permiso, RegisterRequest, Rol } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly url = `${API_BASE}/auth`;

  private readonly _token = signal<string | null>(this.obtenerStorage('mhm_token'));
  private readonly _email = signal<string | null>(this.obtenerStorage('mhm_email'));
  private readonly _rol = signal<Rol | null>(this.obtenerStorage('mhm_rol') as Rol | null);
  private readonly _permisos = signal<Permiso[]>(this.obtenerPermisosStorage());

  readonly token = this._token.asReadonly();
  readonly email = this._email.asReadonly();
  readonly rol = this._rol.asReadonly();
  readonly permisos = this._permisos.asReadonly();

  readonly estaAutenticado = computed(() => !!this._token());
  readonly esAdmin = computed(() => this._rol() === 'ADMIN');

  login(credenciales: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/login`, credenciales).pipe(
      tap(res => this.guardarSesion(res))
    );
  }

  registrar(datos: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/registro`, datos).pipe(
      tap(res => this.guardarSesion(res))
    );
  }

  private guardarSesion(res: AuthResponse): void {
    this._token.set(res.token);
    this._email.set(res.email);
    this._rol.set(res.rol);
    this._permisos.set(res.permisos || []);

    localStorage.setItem('mhm_token', res.token);
    localStorage.setItem('mhm_email', res.email);
    localStorage.setItem('mhm_rol', res.rol);
    localStorage.setItem('mhm_permisos', JSON.stringify(res.permisos || []));
  }

  cerrarSesion(): void {
    this._token.set(null);
    this._email.set(null);
    this._rol.set(null);
    this._permisos.set([]);

    localStorage.removeItem('mhm_token');
    localStorage.removeItem('mhm_email');
    localStorage.removeItem('mhm_rol');
    localStorage.removeItem('mhm_permisos');

    this.router.navigate(['/login']);
  }

  tienePermiso(permiso: Permiso): boolean {
    if (this._rol() === 'ADMIN') return true;
    return this._permisos().includes(permiso);
  }

  mensajeError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) return 'Sin conexión al servidor Spring Boot en http://localhost:8080';
      if (error.status === 401) return '401 Unauthorized: Credenciales incorrectas o sesión expirada.';
      if (error.status === 403) return '403 Forbidden: No tienes permisos suficientes para realizar esta acción.';
      if (error.status === 404) return '404 Not Found: El recurso solicitado no existe.';
      if (error.status === 409) return '409 Conflict: El registro ya existe o genera conflicto de datos/stock.';
      if (error.status === 400) {
        if (error.error && typeof error.error === 'object' && error.error.message) {
          return `400 Bad Request: ${error.error.message}`;
        }
        return '400 Bad Request: Revisa los datos ingresados en el formulario.';
      }
      return `Error del servidor HTTP ${error.status}`;
    }
    return 'Ocurrió un error inesperado al procesar la solicitud.';
  }

  private obtenerStorage(clave: string): string | null {
    try {
      return localStorage.getItem(clave);
    } catch {
      return null;
    }
  }

  private obtenerPermisosStorage(): Permiso[] {
    try {
      const data = localStorage.getItem('mhm_permisos');
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }
}
