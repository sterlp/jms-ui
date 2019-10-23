import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Pageable, SortDirection, Sort } from './pageable.model';

describe('Spring Pageable', () => {

  it('Pageable simple string query', () => {
    const p = new Pageable().asc('foo');
    p.size = 10;
    expect(p.buildQuery()).toBe('page=0&size=10&sort=foo,asc');

    p.page = 3;
    expect(p.buildQuery()).toBe('page=3&size=10&sort=foo,asc');
  });

  it('Pageable newHttpParams', () => {
    const p = new Pageable().desc('foo');
    p.page = 10;
    p.size = 33;

    const params = p.newHttpParams();
    expect(params.get('page')).toBe('10');
    expect(params.get('size')).toBe('33');
    expect(params.get('sort')).toBe('foo,desc');
  });

});
