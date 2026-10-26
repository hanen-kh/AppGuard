import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TaglistComponent } from './taglist.component';

describe('TaglistComponent', () => {
  let component: TaglistComponent;
  let fixture: ComponentFixture<TaglistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TaglistComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TaglistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
