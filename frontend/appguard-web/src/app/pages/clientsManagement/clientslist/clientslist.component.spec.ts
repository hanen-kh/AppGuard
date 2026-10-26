import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientslistComponent } from './clientslist.component';

describe('ClientslistComponent', () => {
  let component: ClientslistComponent;
  let fixture: ComponentFixture<ClientslistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ClientslistComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientslistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
