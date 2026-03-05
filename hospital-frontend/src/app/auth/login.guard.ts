import { inject } from '@angular/core'; 
import { CanActivateFn, Router } from '@angular/router'; 
import { AuthService } from '../auth.service';


export const loginGuard: CanActivateFn = (route, state) => {
  
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.getPacienteId()) {
    
    console.log('LoginGuard: Usuario ya logueado, redirigiendo a /dashboard');
    router.navigate(['/dashboard']); 
    return false; 
  } else {
    
    console.log('LoginGuard: Usuario no logueado, permitiendo acceso a /login');
    return true; 
  }
};