import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { Permiso, Usuario, UsuarioCreateRequest, UsuarioUpdateRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE}/usuarios`;

  private readonly _usuarios = signal<Usuario[]>([]);
  private readonly _permisosCatalogo = signal<Permiso[]>([]);

  readonly usuarios = this._usuarios.asReadonly();
  readonly permisosCatalogo = this._permisosCatalogo.asReadonly();
  readonly cargando = signal(false);

  cargarUsuarios(): void {
    this.cargando.set(true);
    this.http.get<Usuario[]>(this.url).subscribe({
      next: (users) => {
        this._usuarios.set(users);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });

    this.http.get<Permiso[]>(`${this.url}/permisos`).subscribe({
      next: (permisos) => this._permisosCatalogo.set(permisos)
    });
  }

  crear(nuevo: UsuarioCreateRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, nuevo).pipe(
      tap((u) => this._usuarios.update((actual) => [...actual, u]))
    );
  }

  actualizarPermisos(id: number, permisos: Permiso[]): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.url}/${id}/permisos`, { permisos }).pipe(
      tap((u) => {
        this._usuarios.update((actual) => actual.map((item) => (item.id === id ? u : item)));
      })
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`).pipe(
      tap(() => {
        this._usuarios.update((actual) => actual.filter((item) => item.id !== id));
      })
    );
  }
}
