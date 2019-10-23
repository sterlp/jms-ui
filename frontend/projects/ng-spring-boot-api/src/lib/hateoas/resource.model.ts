// WIP // stopped right now

export interface Resources<T> {
    // tslint:disable-next-line: variable-name
    _embedded?: T;
    page: ResourcePage;
}
export interface ResourcePage {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}
