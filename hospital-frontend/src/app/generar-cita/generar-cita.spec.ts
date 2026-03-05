import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenerarCita } from './generar-cita';

describe('GenerarCita', () => {
  let component: GenerarCita;
  let fixture: ComponentFixture<GenerarCita>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenerarCita]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenerarCita);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
