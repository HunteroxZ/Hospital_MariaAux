import { TestBed } from '@angular/core/testing';

import { PacienteAdminService } from './paciente-admin.service';

describe('PacienteAdminService', () => {
  let service: PacienteAdminService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PacienteAdminService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
