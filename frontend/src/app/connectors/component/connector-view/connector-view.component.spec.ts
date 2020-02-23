import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectorViewComponent } from './connector-view.component';

describe('ConnectorViewComponent', () => {
  let component: ConnectorViewComponent;
  let fixture: ComponentFixture<ConnectorViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectorViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectorViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
