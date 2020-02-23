import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { MatDialog } from '@angular/material';
import { Closable, Page, Pageable, EMPTY_PAGE } from 'projects/ng-spring-boot-api/src/public-api';
import { HttpClient } from '@angular/common/http';
import { LoadingService } from 'src/app/common/loading/loading.service';
import { Bookmark } from './bookmarks.model';
import { finalize, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
// tslint:disable: curly
export class BookmarksService {
    private readonly baseUrl = 'api/jms/bookmarks';
    loading$: Observable<boolean>;

    constructor(private http: HttpClient, private loading: LoadingService) {
        this.loading$ = loading.loading$;
    }

    list(connectorId: number, pageable: Pageable): Observable<Page<Bookmark>> {
        this.loading.isLoading();
        return this.http.get<Page<Bookmark>>(`${this.baseUrl}/${connectorId}`, {
                params: pageable ? pageable.newHttpParams() : null
            })
            .pipe(
                finalize(() => this.loading.finishedLoading()),
                catchError(this.loading.handleError<Page<Bookmark>>('Failed to load Bookmarks', EMPTY_PAGE))
            );
    }

    save(connectorId: number, data: Bookmark): Observable<Bookmark> {
        this.loading.isLoading();
        return this.http.post<Bookmark>(`${this.baseUrl}/${connectorId}`, data)
            .pipe(
                finalize(() => this.loading.finishedLoading()),
                catchError(this.loading.handleError<Bookmark>('Failed to save Bookmark', data))
            );
    }

    delete(connectorId: number, id: number): Observable<void> {
        this.loading.isLoading();
        return this.http.delete<void>(`${this.baseUrl}/${connectorId}/${id}`)
            .pipe(
                finalize(() => this.loading.finishedLoading()),
                catchError(this.loading.handleError<void>('Failed to delete Bookmark', null))
            );
    }

}
