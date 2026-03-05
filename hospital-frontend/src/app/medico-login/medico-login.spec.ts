import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicoLogin } from './medico-login';

describe('MedicoLogin', () => {
  let component: MedicoLogin;
  let fixture: ComponentFixture<MedicoLogin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicoLogin]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MedicoLogin);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
