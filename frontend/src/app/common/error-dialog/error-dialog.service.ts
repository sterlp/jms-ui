import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, of } from 'rxjs';
import { ErrorDialogComponent } from './error-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class ErrorDialogService {

    constructor(private dialog: MatDialog) { }

    showError<T>(operation = '', result?: T) {
        return (error: any): Observable<T> => {
            console.error(`${operation}: ${error.message}`, error); // log to console instead
            this.dialog.open(ErrorDialogComponent, {
                width: '70%',
                data: {error, operation},
                closeOnNavigation: true
            });
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

    openError(operation: string, error: any) {
        this.dialog.open(ErrorDialogComponent, {
            width: '70%',
            data: {error, operation},
            closeOnNavigation: true
        });
    }
}
