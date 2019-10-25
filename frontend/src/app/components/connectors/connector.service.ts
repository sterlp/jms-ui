import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportedConnector, ConnectorData, ConnectorView } from 'src/app/api/connector';
import { ArrayUtils } from 'src/app/common/utils';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';
import { LoadingService } from 'src/app/common/loading/loading.service';
import { Page, Pageable, EMPTY_PAGE } from 'projects/ng-spring-boot-api/src/public-api';


@Injectable({
  providedIn: 'root'
})
export class ConnectorService {
  supportedConnectors = new BehaviorSubject<SupportedConnector[]>([]);
  supportedConnectors$ = this.supportedConnectors.asObservable();

  constructor(private http: HttpClient, private loading: LoadingService) { }

  getSupported(): Observable<SupportedConnector[]> {
    if (this.supportedConnectors.value.length === 0) {
      this.reloadSupportedConnectors();
    }
    return this.supportedConnectors$;
  }
  reloadSupportedConnectors(): Observable<SupportedConnector[]> {
    this.loading.isLoading();
    this.http.get<SupportedConnector[]>('api/connectors')
      .pipe(
        finalize(() => this.loading.finishedLoading()),
        catchError(this.loading.handleError<SupportedConnector[]>('Load Supported Connectors', []))
      )
      .subscribe(result => this.supportedConnectors.next(result));
    return this.supportedConnectors$;
  }

  save(data: ConnectorData): Observable<ConnectorData> {
    this.loading.isLoading();
    return this.http.post<ConnectorData>('api/jms/connections', data)
      .pipe(
        finalize(() => this.loading.finishedLoading()),
        catchError(this.loading.handleError<ConnectorData>('Save JMS Connector', data))
      );
  }

  delete(id: number) {
    this.loading.isLoading();
    return this.http.delete(`api/jms/connections/${id}`)
      .pipe(
        finalize(() => this.loading.finishedLoading()),
        catchError(this.loading.handleError<ConnectorData>('Delete JMS Connector', null))
      );
  }

  getConnectorWithConfig(id: number): Observable<ConnectorData> {
    return this.http.get<ConnectorData>(`api/jms/connections/${id}`)
      .pipe(
        finalize(() => this.loading.finishedLoading()),
        catchError(this.loading.handleError<ConnectorData>('Load JMS Connector', null))
      );
  }

  /**
   * Returns a Hateos Resource containing 
   * <li> _embedded.jmsConnections
   * <li> page
   */
  listConnections(pageable: Pageable): Observable<Page<ConnectorView>> {
    return this.http.get<Page<ConnectorView>>('api/jms/connections', {
      params: pageable ? pageable.newHttpParams() : null
    })
    .pipe(
      finalize(() => this.loading.finishedLoading()),
      catchError(this.loading.handleError<Page<ConnectorView>>('Load JMS Connectors', null))
    );
  }
}

export class ConnertorViewDataSource implements DataSource<ConnectorView> {
  constructor(private $connector: ConnectorService, private $loading: LoadingService) {}

  private connectorDataSubject = new BehaviorSubject<ConnectorView[]>([]);
  private pageSubject = new BehaviorSubject<Page<ConnectorView>>(EMPTY_PAGE);

  public loading$ = this.$loading.loading$;
  public page$ = this.pageSubject.asObservable();

  connect(collectionViewer: CollectionViewer): Observable<ConnectorView[] | readonly ConnectorView[]> {
    return this.connectorDataSubject.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.connectorDataSubject.complete();
    this.pageSubject.complete();
  }

  loadConnectorData(page: number = 0, size: number = 10): void {
    console.info('loadConnectorData ...');
    this.$loading.isLoading();
    this.$connector.listConnections(Pageable.of(page, size))
      .pipe(finalize(() => this.$loading.finishedLoading()))
      .subscribe(data => {
        this.pageSubject.next(data);
        this.connectorDataSubject.next(data.content ? data.content : []);
      });
  }
}
