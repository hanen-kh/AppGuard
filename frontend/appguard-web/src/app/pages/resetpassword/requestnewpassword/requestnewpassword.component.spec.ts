import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RequestnewpasswordComponent } from './requestnewpassword.component';

describe('RequestnewpasswordComponent', () => {
  let component: RequestnewpasswordComponent;
  let fixture: ComponentFixture<RequestnewpasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RequestnewpasswordComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RequestnewpasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
