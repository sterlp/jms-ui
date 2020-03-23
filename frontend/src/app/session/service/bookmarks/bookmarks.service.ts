import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { Bookmark } from './bookmarks.model';
import { finalize, catchError } from 'rxjs/operators';
import { Pageable, SpringResource, Page } from '@sterlp/ng-spring-boot-api';

@Injectable({
  providedIn: 'root'
})
// tslint:disable: curly
export class BookmarksService extends SpringResource<Page<Bookmark>, Bookmark> {
    readonly listUrl = 'api/bookmarks';

    constructor(protected http: HttpClient) {
        super(http);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.listUrl}/${id}`);
    }

    save(entity: Bookmark, connectorId: number | string): Observable<Bookmark> {
        const id = entity.id ? entity.id : '';
        return this.http.post<Bookmark>(`${this.listUrl}/${connectorId}/${id}`, entity);
    }
}
