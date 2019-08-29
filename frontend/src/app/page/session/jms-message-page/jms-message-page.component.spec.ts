import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsMessagePageComponent } from './jms-message-page.component';

describe('JmsMessagePageComponent', () => {
  let component: JmsMessagePageComponent;
  let fixture: ComponentFixture<JmsMessagePageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsMessagePageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsMessagePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
