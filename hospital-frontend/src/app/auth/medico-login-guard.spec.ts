import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { medicoLoginGuard } from './medico-login-guard';

describe('medicoLoginGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => medicoLoginGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
