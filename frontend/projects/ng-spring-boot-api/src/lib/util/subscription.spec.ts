import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionsHolder, Closable } from './subscription.util';

class Foo implements Closable {
  closed = false;
  close(): void {
    this.closed = true;
  }
}
class HasUnsubscribe {
  closed = false;
  unsubscribe() {
    this.closed = true;
  }
}

describe('SubscriptionsHolder', () => {
  const s = new SubscriptionsHolder();

  it('Close Closeable', () => {
    const a = new Foo();
    s.add(a);
    expect(s.size).toBe(1);

    s.close();
    expect(a.closed).toBe(true);
    expect(s.size).toBe(0);
  });
  it('Close unsubscribe', () => {
    const a = new HasUnsubscribe();
    const b = new Foo();
    s.addAny(a).addAny(b);

    s.close();
    expect(a.closed).toBe(true);
    expect(b.closed).toBe(true);
    expect(s.size).toBe(0);
  });
});
