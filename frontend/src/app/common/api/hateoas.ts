export class Resources<T> {
    // tslint:disable-next-line: variable-name
    _embedded?: T;
    page: Page;
}

export class Page {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}
