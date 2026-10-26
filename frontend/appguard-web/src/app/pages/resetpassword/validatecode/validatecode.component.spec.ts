import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidatecodeComponent } from './validatecode.component';

describe('ValidatecodeComponent', () => {
  let component: ValidatecodeComponent;
  let fixture: ComponentFixture<ValidatecodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ValidatecodeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ValidatecodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
