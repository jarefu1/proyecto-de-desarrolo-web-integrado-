import { Component, input, signal, effect } from '@angular/core';
import { API_BASE } from '../config/api.config';
@Component({selector:'app-producto-imagen',standalone:true,template:`@if(src() && !fallo()){<img [src]="url()" [alt]="alt()" [loading]="prioritaria()?'eager':'lazy'" (error)="fallo.set(true)"/>}@else{<span class="placeholder" role="img" [attr.aria-label]="alt()+' · imagen no disponible'"><svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><rect x="3" y="3" width="18" height="18" rx="3"/><path d="m4 17 5-5 4 4 3-3 5 5"/><circle cx="15" cy="8" r="2"/></svg><span>Imagen no disponible</span></span>}`,styles:[`:host{display:grid;place-items:center;width:100%;height:100%;min-height:0;overflow:hidden}img{width:100%;height:100%;object-fit:contain;display:block}.placeholder{display:grid;place-items:center;gap:.6rem;color:#939393;font-size:.72rem;text-align:center;padding:.7rem}`]})
export class ProductoImagenComponent {
 readonly src=input<string|null|undefined>(); readonly alt=input('Mochila'); readonly prioritaria=input(false); readonly fallo=signal(false);
 constructor(){effect(()=>{this.src();this.fallo.set(false);});}
 url():string { const src=this.src()!; return src.startsWith('blob:')?src:new URL(src,new URL(API_BASE,globalThis.location.origin)).href; }
}
