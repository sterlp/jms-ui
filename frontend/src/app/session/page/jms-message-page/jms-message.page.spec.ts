import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsMessagePage } from './jms-message.page';

describe('JmsMessagePage', () => {
  let component: JmsMessagePage;
  let fixture: ComponentFixture<JmsMessagePage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsMessagePage ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsMessagePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
