import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsHeadersComponent } from './jms-headers.component';

describe('JmsHeadersComponent', () => {
  let component: JmsHeadersComponent;
  let fixture: ComponentFixture<JmsHeadersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsHeadersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsHeadersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
