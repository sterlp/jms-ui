import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenSessionsComponent } from './open-sessions.component';

describe('OpenSessionsComponent', () => {
  let component: OpenSessionsComponent;
  let fixture: ComponentFixture<OpenSessionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenSessionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
