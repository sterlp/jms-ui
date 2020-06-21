import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectorsPage } from './connectors.page';

describe('ConnectorsComponent', () => {
  let component: ConnectorsPage;
  let fixture: ComponentFixture<ConnectorsPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectorsPage ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectorsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
