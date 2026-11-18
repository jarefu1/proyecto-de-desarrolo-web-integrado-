import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductosService } from '../../services/productos.service';
import { ProductoImagenComponent } from '../../components/producto-imagen.component';
import { RevelarDirective } from '../../components/revelar.directive';
@Component({selector:'app-inicio',standalone:true,imports:[CommonModule,RouterLink,ProductoImagenComponent,RevelarDirective],providers:[ProductosService],templateUrl:'./inicio.page.html',styleUrl:'./inicio.page.css'})
export class InicioPage implements OnInit,OnDestroy {
 readonly productos=inject(ProductosService);readonly indice=signal(0);readonly pausado=signal(false);readonly automatico=signal(true);
 readonly diapositivas=computed(()=>{const p=this.productos.productos();return [...p.filter(x=>!!x.imagenUrl),...p.filter(x=>!x.imagenUrl)].slice(0,6);});
 readonly destacada=computed(()=>this.diapositivas()[this.indice()%Math.max(1,this.diapositivas().length)]);
 private timer:ReturnType<typeof setInterval>|undefined;private media:MediaQueryList|undefined;
 private cambioMovimiento=(e:MediaQueryListEvent)=>{if(e.matches)this.automatico.set(false);};
 ngOnInit():void{this.productos.cargarTodo();this.media=matchMedia('(prefers-reduced-motion: reduce)');this.automatico.set(!this.media.matches);this.media.addEventListener('change',this.cambioMovimiento);this.timer=setInterval(()=>{if(this.automatico()&&!this.pausado()&&document.visibilityState==='visible'&&this.diapositivas().length>1)this.avanzar(1);},6500);}
 avanzar(delta:number):void{const total=this.diapositivas().length;if(total)this.indice.set((this.indice()+delta+total)%total);}
 ir(i:number):void{this.indice.set(i);}
 ngOnDestroy():void{clearInterval(this.timer);this.media?.removeEventListener('change',this.cambioMovimiento);}
}
