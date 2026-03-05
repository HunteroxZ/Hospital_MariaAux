import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { MedicoAuthService } from './medico-auth.service';

export const medicoLoginGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const medicoAuthService = inject(MedicoAuthService); 

  if (medicoAuthService.isLoggedIn()) { 
    
    router.navigate(['/medico/dashboard']); 
    return false; 
  } else {
    return true; 
  }
};