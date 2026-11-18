import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class FavoritosService {
  private readonly key = 'mhm_favoritos';
  readonly ids = signal<number[]>(this.leer());
  contiene(id: number): boolean { return this.ids().includes(id); }
  alternar(id: number): void { const next = this.contiene(id) ? this.ids().filter(x => x !== id) : [...this.ids(), id]; this.ids.set(next); localStorage.setItem(this.key, JSON.stringify(next)); }
  private leer(): number[] { try { const value = JSON.parse(localStorage.getItem(this.key) ?? '[]'); return Array.isArray(value) ? value.filter(Number.isFinite) : []; } catch { return []; } }
}
