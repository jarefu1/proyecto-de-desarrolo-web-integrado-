import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/producto.model';
import { ProductosService } from '../../services/productos.service';
import { FavoritosService } from '../../services/favoritos.service';
import { API_BASE } from '../../config/api.config';
@Component({selector:'app-favoritos',standalone:true,imports:[CommonModule,RouterLink],template:`
<section class="page"><span class="eyebrow">TU SELECCIÓN</span><h1>Favoritos</h1>
@if(!productos().length){<div class="empty"><p>Aún no guardaste mochilas favoritas.</p><a routerLink="/productos">Explorar catálogo</a></div>}
<div class="grid">@for(p of productos();track p.id){<article class="card"><a [routerLink]="['/producto',p.id]" class="image">@if(p.imagenUrl){<img [src]="imagenCompleta(p.imagenUrl)" [alt]="p.modelo"/>}@else{<span>▧<small>Imagen no disponible</small></span>}</a><button (click)="quitar(p.id)" aria-label="Quitar de favoritos">♥</button><a [routerLink]="['/producto',p.id]" class="name">{{p.modelo}}</a><small>{{p.color}} · {{p.categoriaNombre}}</small><strong>S/ {{p.precio | number:'1.2-2'}}</strong></article>}</div></section>
`,styles:[`.page{padding:2rem 0 4rem}.eyebrow{font-size:.7rem;color:#777;letter-spacing:.15em}.page h1{font-size:3rem;letter-spacing:-.06em;margin:.5rem 0 2rem}.empty{padding:3rem;border:1px solid #e5e5e5;border-radius:18px;color:#777}.empty a{color:#111}.grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:1.25rem}.card{position:relative;display:grid;gap:.5rem}.image{height:260px;border-radius:18px;background:#f1f1f1;display:grid;place-items:center;overflow:hidden}.image img{width:100%;height:100%;object-fit:contain;mix-blend-mode:multiply}.image span{display:grid;justify-items:center;color:#999;font-size:2rem}.image small{font-size:.75rem}.card button{position:absolute;right:12px;top:12px;border:0;border-radius:50%;width:38px;height:38px;background:#fff;color:#e44;cursor:pointer}.name{text-decoration:none;color:#111;font-weight:700}.card>small{color:#777}.card strong{font-size:1.2rem}@media(max-width:800px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}.image{height:200px}}` ]})
export class FavoritosPage implements OnInit {
 private readonly favoritos=inject(FavoritosService);private readonly productosService=inject(ProductosService);readonly productos=signal<Producto[]>([]);
 ngOnInit():void{this.cargar();}
 private cargar():void{this.productos.set([]);for(const id of this.favoritos.ids())this.productosService.obtenerPorId(id).subscribe({next:p=>this.productos.update(items=>[...items,p]),error:()=>this.favoritos.alternar(id)});}
 quitar(id:number):void{this.favoritos.alternar(id);this.productos.update(items=>items.filter(p=>p.id!==id));}
 imagenCompleta(url:string):string{return `${API_BASE.replace('/api/v1','')}${url}`;}
}
