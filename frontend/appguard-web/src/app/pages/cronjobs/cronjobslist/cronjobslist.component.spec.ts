import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CronjobslistComponent } from './cronjobslist.component';

describe('CronjobslistComponent', () => {
  let component: CronjobslistComponent;
  let fixture: ComponentFixture<CronjobslistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CronjobslistComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CronjobslistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
