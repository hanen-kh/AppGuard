import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CronhistoryComponent } from './cronhistory.component';

describe('CronhistoryComponent', () => {
  let component: CronhistoryComponent;
  let fixture: ComponentFixture<CronhistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CronhistoryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CronhistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
