
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { RecepcionistaService } from '../services/recepcionista.service';

export const recepcionistaGuard = () => {
  const recepcionistaService = inject(RecepcionistaService);
  const router = inject(Router);

  if (recepcionistaService.estaLogueado()) {
    return true;
  }

  router.navigate(['/recepcionista/login']);
  return false;
};


export const recepcionistaLoginGuard = () => {
  const recepcionistaService = inject(RecepcionistaService);
  const router = inject(Router);

  if (!recepcionistaService.estaLogueado()) {
    return true;
  }

  router.navigate(['/recepcionista/dashboard']);
  return false;
};