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
import { Page } from 'projects/ng-spring-boot-api/src/public-api';

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

  getStoredSession(connectorId: number, connectors?: ConnectorView[]): ConnectorView {
    // tslint:disable-next-line: curly
    if (!connectors) connectors = this.sessions$.value;
    return connectors.find(s => s.id === connectorId);
  }

  /**
   * Opens a session to the given connector.
   * @returns subject of all currently open sessions
   */
  openSession(connectorId: number): Observable<ConnectorView[]> {
    this.$loading.isLoading();
    return this.http.post<Page<ConnectorView>>(`/api/jms/sessions/${connectorId}`, null)
      .pipe(
        finalize(() => this.$loading.finishedLoading()),
        catchError(this.handleError<Page<ConnectorView>>('Faild to connect', null)),
        map(s => {
          if (s && s.content) {
            this.sessions$.next(s.content);
            return s.content;
          }
          return [];
        })
      );
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
