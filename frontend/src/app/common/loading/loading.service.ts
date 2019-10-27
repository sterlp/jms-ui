import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ErrorDialogComponent } from '../error-dialog/error-dialog.component';
import { MatDialog } from '@angular/material';
import { Closable } from 'projects/ng-spring-boot-api/src/public-api';

@Injectable({
  providedIn: 'root'
})
export class LoadingService implements Closable, OnDestroy {

  // tslint:disable-next-line: variable-name
  private _loading = new BehaviorSubject<boolean>(false);
  public readonly loading$ = this._loading.asObservable();

  constructor(private dialog: MatDialog) {}

  isLoading() {
    this._loading.next(true);
  }
  finishedLoading() {
    this._loading.next(false);
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`, error); // log to console instead
      this.dialog.open(ErrorDialogComponent, {
        width: '60%',
        data: {error, operation},
        closeOnNavigation: true
      });
      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  close(): void {
    this._loading.complete();
  }
  ngOnDestroy(): void {
    this.close();
  }
}
