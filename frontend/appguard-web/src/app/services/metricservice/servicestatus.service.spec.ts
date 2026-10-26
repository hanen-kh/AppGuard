import { TestBed } from '@angular/core/testing';

import { ServicestatusService } from './servicestatus.service';

describe('ServicestatusService', () => {
  let service: ServicestatusService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ServicestatusService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
