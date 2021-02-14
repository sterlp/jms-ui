import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsResourceDetailsComponent } from './jms-resource-details.component';

describe('JmsResourceDetailsComponent', () => {
  let component: JmsResourceDetailsComponent;
  let fixture: ComponentFixture<JmsResourceDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsResourceDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsResourceDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
