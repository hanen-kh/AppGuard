import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddcronjobComponent } from './addcronjob.component';

describe('AddcronjobComponent', () => {
  let component: AddcronjobComponent;
  let fixture: ComponentFixture<AddcronjobComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddcronjobComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddcronjobComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
