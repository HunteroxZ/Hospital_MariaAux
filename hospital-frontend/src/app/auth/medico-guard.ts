import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { MedicoAuthService } from './medico-auth.service';



export const medicoGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const medicoAuthService = inject(MedicoAuthService); 

  if (medicoAuthService.isLoggedIn()) { 
    return true; 
  } else {
    router.navigate(['/medico/login']); 
    return false;
  }
};