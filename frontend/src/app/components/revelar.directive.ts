import { AfterViewInit, Directive, ElementRef, OnDestroy, inject } from '@angular/core';
@Directive({selector:'[appRevelar]',standalone:true})
export class RevelarDirective implements AfterViewInit,OnDestroy {
 private readonly elemento=inject(ElementRef<HTMLElement>);private observer?:IntersectionObserver;
 ngAfterViewInit():void{if(!('IntersectionObserver' in globalThis)||matchMedia('(prefers-reduced-motion: reduce)').matches)return;const el=this.elemento.nativeElement;el.classList.add('reveal-ready');this.observer=new IntersectionObserver(entries=>{if(entries.some(e=>e.isIntersecting)){el.classList.add('reveal-visible');this.observer?.disconnect();}},{threshold:.08});this.observer.observe(el);}
 ngOnDestroy():void{this.observer?.disconnect();}
}
