import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Connector.PageComponent } from './connector.page.component';

describe('Connector.PageComponent', () => {
  let component: Connector.PageComponent;
  let fixture: ComponentFixture<Connector.PageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Connector.PageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Connector.PageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
