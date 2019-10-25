import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { ConnectorData, ConnectorView } from 'src/app/api/connector';
import { JmsResource, SendJmsMessageCommand, JmsResultMessage } from 'src/app/api/jms-session';
import { ArrayUtils } from 'src/app/common/utils';
import { LoadingService } from 'src/app/common/loading/loading.service';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject, Subscription } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { ErrorDialogComponent } from 'src/app/common/error-dialog/error-dialog.component';

// /api/jms/sessions
@Injectable({
  providedIn: 'root'
})
export class JmsSessionService {

  public sessions$ = new BehaviorSubject<ConnectorView[]>([]);
  public loading$: Observable<boolean>;

  constructor(private http: HttpClient, private $loading: LoadingService, private dialog: MatDialog) {
    this.loading$ = $loading.loading$;
  }
  /**
   * Opens a session to the given connector.
   * @returns subject of all currently open sessions
   */
  openSession(connector: ConnectorView): Observable<ConnectorView[]> {
    this.$loading.isLoading();
    this.http.post<number>(`/api/jms/sessions/${connector.id}`, null)
      .pipe(
        finalize(() => this.$loading.finishedLoading()),
        catchError(this.handleError<number>('Faild to connect', null))
      )
      .subscribe(id => {
        if (id) {
          const current = this.sessions$.getValue();
          if (current.filter(s => s.id === id).length === 0) {
            current.push(connector);
            this.sessions$.next(current);
          }
        }
      });
    return this.sessions$;
  }

  sendJmsMessage(connectorId: number, target: string, body: SendJmsMessageCommand): Observable<void> {
    this.$loading.isLoading();
    return this.http.post<void>(`api/jms/sessions/${connectorId}/message/${target}`, body)
                    .pipe(
                      finalize(() => this.$loading.finishedLoading()),
                      catchError(this.handleError<void>('Send JMS Message to ' + target, null))
                    );
  }

  receiveJmsMessage(connectorId: number, target: string): Observable<JmsResultMessage> {
    this.$loading.isLoading();
    return this.http.get<JmsResultMessage>(`api/jms/sessions/${connectorId}/message/${target}`).pipe(
        finalize(() => this.$loading.finishedLoading()),
        catchError(this.handleError<JmsResultMessage>('Send JMS Message to ' + target, null))
      );
  }

  getQueues(connectorId: number): Observable<JmsResource[]> {
    this.$loading.isLoading();
    return this.http.get<JmsResource[]>('api/jms/sessions/' + connectorId + '/queues')
               .pipe(finalize(() => this.$loading.finishedLoading()));
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`, error); // log to console instead
      this.dialog.open(ErrorDialogComponent, {
        width: '80%',
        data: {error, operation},
        closeOnNavigation: true
      });
      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
