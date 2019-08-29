import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportedConnector, ConnectorData, ConnectorDataResource } from 'src/app/api/connector';
import { Resources, Page } from 'src/app/common/api/hateoas';
import { ArrayUtils } from 'src/app/common/utils';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';
import { LoadingService } from 'src/app/common/loading/loading.service';


@Injectable({
  providedIn: 'root'
})
export class ConnectorService {

  constructor(private http: HttpClient, private loading: LoadingService) { }

  getSupported(): Observable<SupportedConnector[]> {
    return this.http.get<SupportedConnector[]>('api/connectors')
      .pipe(
        finalize(() => this.loading.finishedLoading()),
        catchError(this.loading.handleError<SupportedConnector[]>('Load Supported Connectors', []))
      );
  }

  save(data: ConnectorData): Observable<ConnectorData> {
    return this.http.post<ConnectorData>('api/jms/connections', data);
  }

  getConnectorWithConfig(id: number): Observable<ConnectorData> {
    return this.http.get<ConnectorData>('api/jms/connections/' + id);
  }

  /**
   * Returns a Hateos Resource containing 
   * <li> _embedded.jmsConnections
   * <li> page
   */
  listConnections(page: number = 0, size: number = 10): Observable<Resources<ConnectorDataResource>> {
    return this.http.get<Resources<ConnectorDataResource>>('api/jms-connections', {
      params: new HttpParams()
        .set('page', page.toString()).set('size', size.toString())
    });
  }
}

export class ConnertorDataSource implements DataSource<ConnectorData> {
  constructor(private $connector: ConnectorService, private $loading: LoadingService) {}

  private connectorDataSubject = new BehaviorSubject<ConnectorData[]>([]);
  private pageSubject = new BehaviorSubject<Page>(new Page());

  public loading$ = this.$loading.loading$;
  public page$ = this.pageSubject.asObservable();

  connect(collectionViewer: CollectionViewer): Observable<ConnectorData[] | readonly ConnectorData[]> {
    return this.connectorDataSubject.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.connectorDataSubject.complete();
    this.pageSubject.complete();
  }

  loadConnectorData(page: number = 0, size: number = 10): void {
    console.info('loadConnectorData ...');
    this.$loading.isLoading();
    this.$connector.listConnections(page, size)
      .pipe(finalize(() => this.$loading.finishedLoading()))
      .subscribe(data => {
        this.pageSubject.next(data.page);
        this.connectorDataSubject.next(data._embedded ? data._embedded.jmsConnections : []);
      });
  }
}
