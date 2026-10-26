import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddclientComponent } from './addclient.component';

describe('AddclientComponent', () => {
  let component: AddclientComponent;
  let fixture: ComponentFixture<AddclientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddclientComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddclientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
