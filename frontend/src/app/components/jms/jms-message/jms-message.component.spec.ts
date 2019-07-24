import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsMessageComponent } from './jms-message.component';

describe('JmsMessageComponent', () => {
  let component: JmsMessageComponent;
  let fixture: ComponentFixture<JmsMessageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsMessageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
