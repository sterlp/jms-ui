import { TestBed } from '@angular/core/testing';

import { JmsSessionService } from './jms-session.service';

describe('JmsSessionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: JmsSessionService = TestBed.get(JmsSessionService);
    expect(service).toBeTruthy();
  });
});
