import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsResourceListComponent } from './jms-resource-list.component';

describe('JmsResourceListComponent', () => {
  let component: JmsResourceListComponent;
  let fixture: ComponentFixture<JmsResourceListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsResourceListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsResourceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
