import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicoHistorialBuscarComponent } from './medico-historial-buscar.component';

describe('MedicoHistorialBuscar', () => {
  let component: MedicoHistorialBuscarComponent;
  let fixture: ComponentFixture<MedicoHistorialBuscarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicoHistorialBuscarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicoHistorialBuscarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
