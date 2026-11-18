import { Component, OnInit, inject, signal, computed, DestroyRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap, tap, catchError, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProductosService } from '../../services/productos.service';
import { AuthService } from '../../services/auth.service';
import { CarritoPedidoService } from '../../services/carrito-pedido.service';
import { FavoritosService } from '../../services/favoritos.service';
import { DetalleProducto, Producto } from '../../models/producto.model';
import { ProductoImagenComponent } from '../../components/producto-imagen.component';
@Component({selector:'app-producto-detalle',standalone:true,imports:[CommonModule,RouterLink,ProductoImagenComponent],templateUrl:'./producto-detalle.page.html',styleUrl:'./producto-detalle.page.css'})
export class ProductoDetallePage implements OnInit {
 private readonly route=inject(ActivatedRoute);private readonly router=inject(Router);private readonly productos=inject(ProductosService);private readonly carrito=inject(CarritoPedidoService);private readonly destroy=inject(DestroyRef);
 readonly auth=inject(AuthService);readonly favoritos=inject(FavoritosService);
 readonly detalle=signal<DetalleProducto|null>(null);readonly cargando=signal(true);readonly mensaje=signal('');readonly cantidad=signal(1);readonly enviando=signal(false);readonly indice=signal(0);readonly ampliada=signal(false);
 readonly producto=computed(()=>this.detalle()?.producto??null);
 readonly fotos=computed(()=>{const d=this.detalle();if(!d)return [];return [...(d.producto.imagenUrl?[d.producto.imagenUrl]:[]),...d.imagenes.map(i=>i.url)];});
 readonly especificaciones=computed(()=>{const p=this.producto();return p?[{nombre:'Material',valor:p.material},{nombre:'Capacidad',valor:p.capacidad},{nombre:'Dimensiones',valor:p.dimensiones},{nombre:'Peso',valor:p.peso},{nombre:'Color',valor:p.color},{nombre:'Categoría',valor:p.categoriaNombre},{nombre:'Código de producto',valor:p.codigo}].filter(x=>!!x.valor):[];});
 ngOnInit():void{this.route.paramMap.pipe(tap(()=>{this.cargando.set(true);this.detalle.set(null);this.mensaje.set('');this.cantidad.set(1);this.indice.set(0);this.ampliada.set(false);}),switchMap(params=>this.productos.obtenerDetalle(Number(params.get('id'))).pipe(catchError(()=>of(null)))),takeUntilDestroyed(this.destroy)).subscribe(d=>{this.detalle.set(d);this.cargando.set(false);});}
 seleccionar(p:Producto):void{if(p.id!==this.producto()?.id)this.router.navigate(['/producto',p.id]);}
 color(p:Producto):string{if(p.colorHex)return p.colorHex;const colores:Record<string,string>={negro:'#202020',blanco:'#f5f5f5',azul:'#354e7c',rojo:'#a83c3c',verde:'#596c53',gris:'#8a8a8a',rosado:'#dca6b5',rosa:'#dca6b5',beige:'#d2c3a9',marron:'#82654f','marrón':'#82654f',amarillo:'#e2be4b',morado:'#775b91',naranja:'#ce7c3b'};return colores[p.color.toLocaleLowerCase('es').trim()]??'#b5b5b5';}
 ajustar(delta:number):void{this.cantidad.update(q=>Math.max(1,Math.min(this.producto()?.cantidad??1,q+delta)));}
 agregar():void{const p=this.producto();if(!p||this.enviando()||p.cantidad<1)return;if(!this.auth.estaAutenticado()){this.router.navigate(['/login'],{queryParams:{returnUrl:'/producto/'+p.id}});return;}this.enviando.set(true);this.carrito.agregarItem(p.id,this.cantidad()).subscribe({next:()=>{this.enviando.set(false);this.mensaje.set(`${this.cantidad()} unidad(es) en color ${p.color} añadidas a tu carrito.`);},error:e=>{this.enviando.set(false);this.mensaje.set(this.auth.mensajeError(e));}});}
 @HostListener('document:keydown.escape') cerrarZoom():void{this.ampliada.set(false);}
}
