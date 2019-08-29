import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ErrorDialogComponent } from '../error-dialog/error-dialog.component';
import { MatDialog } from '@angular/material';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  public loading$ = new BehaviorSubject<boolean>(false);

  constructor(private dialog: MatDialog) {}

  isLoading() {
    this.loading$.next(true);
  }
  finishedLoading() {
    this.loading$.next(false);
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
}
