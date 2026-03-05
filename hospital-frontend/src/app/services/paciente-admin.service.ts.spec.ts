import { TestBed } from '@angular/core/testing';

import { PacienteAdminServiceTs } from './paciente-admin.service.ts';

describe('PacienteAdminServiceTs', () => {
  let service: PacienteAdminServiceTs;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PacienteAdminServiceTs);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
