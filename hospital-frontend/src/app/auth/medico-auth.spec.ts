import { TestBed } from '@angular/core/testing';

import { MedicoAuthService } from './medico-auth.service';

describe('MedicoAuth', () => {
  let service: MedicoAuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MedicoAuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
