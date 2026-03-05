import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicoAgenda } from './medico-agenda';

describe('MedicoAgenda', () => {
  let component: MedicoAgenda;
  let fixture: ComponentFixture<MedicoAgenda>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicoAgenda]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicoAgenda);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
