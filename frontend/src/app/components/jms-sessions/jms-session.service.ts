import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportedConnector, ConnectorData, ConnectorDataResource } from 'src/app/api/connector';
import { JmsResource } from 'src/app/api/jms-session';
import { ArrayUtils } from 'src/app/common/utils';
import { LoadingService } from 'src/app/common/loading/loading.service';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject, Subscription } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class JmsSessionService {

  public sessions$ = new BehaviorSubject<ConnectorData[]>([]);
  public loading$: Observable<boolean>;

  constructor(private http: HttpClient, private $loading: LoadingService) {
    this.loading$ = $loading.loading$;
  }

  openSession(session: ConnectorData): Observable<ConnectorData[]> {
    this.$loading.isLoading();
    this.http.post<number>('api/jms/sessions/' + session.id, null)
      .pipe(finalize(() => this.$loading.finishedLoading()))
      .subscribe(r => {
        const current = this.sessions$.getValue();
        if (current.filter(s => s.id === session.id).length === 0) {
          current.push(session);
          this.sessions$.next(current);
        }
      });
    return this.sessions$;
  }

  getQueues(connectorId: number): Observable<JmsResource[]> {
    this.$loading.isLoading();
    return this.http.get<JmsResource[]>('api/jms/sessions/' + connectorId + '/queues')
               .pipe(finalize(() => this.$loading.finishedLoading()));
  }
}
