import { TestBed } from '@angular/core/testing';

import { CronjobService } from './cronjob.service';

describe('CronjobService', () => {
  let service: CronjobService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CronjobService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
