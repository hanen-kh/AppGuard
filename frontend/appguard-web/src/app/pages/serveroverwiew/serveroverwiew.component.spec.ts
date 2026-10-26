import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServeroverwiewComponent } from './serveroverwiew.component';

describe('ServeroverwiewComponent', () => {
  let component: ServeroverwiewComponent;
  let fixture: ComponentFixture<ServeroverwiewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServeroverwiewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServeroverwiewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
