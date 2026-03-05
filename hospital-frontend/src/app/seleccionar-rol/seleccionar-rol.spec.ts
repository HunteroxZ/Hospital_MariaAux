import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeleccionarRol } from './seleccionar-rol';

describe('SeleccionarRol', () => {
  let component: SeleccionarRol;
  let fixture: ComponentFixture<SeleccionarRol>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeleccionarRol]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeleccionarRol);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
