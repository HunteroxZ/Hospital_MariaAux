import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicoHistorialRegistroComponent } from './medico-historial-registro.component';

describe('MedicoHistorialRegistro', () => {
  let component: MedicoHistorialRegistroComponent;
  let fixture: ComponentFixture<MedicoHistorialRegistroComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicoHistorialRegistroComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicoHistorialRegistroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
