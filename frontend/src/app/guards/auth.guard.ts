import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estaAutenticado()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estaAutenticado() && auth.esAdmin()) {
    return true;
  }
  router.navigate(['/productos']);
  return false;
};

export const inventarioGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estaAutenticado() && (auth.tienePermiso('GESTIONAR_PRODUCTOS') || auth.tienePermiso('GESTIONAR_INVENTARIO') || auth.tienePermiso('GESTIONAR_CATALOGOS')) ? true : inject(Router).createUrlTree(['/productos']);
};
