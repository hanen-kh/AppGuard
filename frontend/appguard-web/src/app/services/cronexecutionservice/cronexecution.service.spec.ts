import { TestBed } from '@angular/core/testing';

import { CronexecutionService } from './cronexecution.service';

describe('CronexecutionService', () => {
  let service: CronexecutionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CronexecutionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
