import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectorPage } from './connector.page';

describe('Connector.PageComponent', () => {
  let component: ConnectorPage;
  let fixture: ComponentFixture<ConnectorPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectorPage ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectorPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
