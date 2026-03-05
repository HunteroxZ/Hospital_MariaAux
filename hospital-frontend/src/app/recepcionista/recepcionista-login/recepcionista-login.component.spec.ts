import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecepcionistaLoginComponent } from './recepcionista-login.component';

describe('RecepcionistaLoginComponent', () => {
  let component: RecepcionistaLoginComponent;
  let fixture: ComponentFixture<RecepcionistaLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecepcionistaLoginComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecepcionistaLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
