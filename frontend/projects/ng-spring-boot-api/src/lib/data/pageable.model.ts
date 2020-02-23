import { HttpParams } from '@angular/common/http';

/**
 * Interface for pagination information during an request.
 * ?page=0&size=5&sort=name,desc
 */
export class Pageable {
    page = 0;
    size = 25;
    sort: Sort[] = [];

    static of(page?: number, size?: number): Pageable {
        const result = new Pageable();
        // tslint:disable-next-line: curly
        if (page != null) result.page = page;
        // tslint:disable-next-line: curly
        if (size != null) result.size = size;
        return result;
    }

    asc(field: string): Pageable {
        // tslint:disable-next-line: no-use-before-declare
        return this.addSort(field, SortDirection.ASC);
    }
    desc(field: string): Pageable {
        // tslint:disable-next-line: no-use-before-declare
        return this.addSort(field, SortDirection.DESC);
    }
    addSort(field: string, direction: SortDirection): Pageable {
        this.sort.push({field, direction});
        return this;
    }
    /**
     * Builds: page=0&size=5&sort=name,desc
     */
    buildQuery(): string {
        let query = `page=${this.page}&size=${this.size}`;
        if (this.sort && this.sort.length > 0) {
            this.sort.forEach(s => {
                query += '&sort=' + s.field + ',' + s.direction;
            });
        }
        return query;
    }
    newHttpParams(): HttpParams {
        let result = new HttpParams()
            .set('page', this.page.toString()).set('size', this.size.toString());
        if (this.sort && this.sort.length > 0) {
            this.sort.forEach(s => {
                result = result.append('sort', s.field + ',' + s.direction);
            });
        }
        return result;
    }
    public toString(): string {
        return this.buildQuery();
    }
}
export enum SortDirection {
    ASC = 'asc',
    DESC = 'desc'
}
export interface Sort {
    field: string;
    direction: SortDirection;
}
