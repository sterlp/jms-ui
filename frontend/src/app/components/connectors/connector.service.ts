import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportedConnector, ConnectorData, ConnectorDataResource } from 'src/app/api/connector';
import { Resources, Page } from 'src/app/common/api/hateoas';
import { ArrayUtils } from 'src/app/common/utils';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class ConnectorService {

  constructor(private http: HttpClient) { }

  getSupported(): Observable<SupportedConnector[]> {
    return this.http.get<SupportedConnector[]>('api/connectors');
  }

  save(data: ConnectorData): Observable<ConnectorData> {
    return this.http.post<ConnectorData>('api/jms/connections', data)
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
  constructor(private $connector: ConnectorService) {}

  private connectorDataSubject = new BehaviorSubject<ConnectorData[]>([]);
  private pageSubject = new BehaviorSubject<Page>(new Page());
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public $loading = this.loadingSubject.asObservable();
  public $page = this.pageSubject.asObservable();

  connect(collectionViewer: CollectionViewer): Observable<ConnectorData[] | readonly ConnectorData[]> {
    return this.connectorDataSubject.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.connectorDataSubject.complete();
    this.pageSubject.complete();
    this.loadingSubject.complete();
  }

  loadConnectorData(page: number = 0, size: number = 10): void {
    console.info('loadConnectorData ...');
    this.loadingSubject.next(true);
    this.$connector.listConnections(page, size)
      .pipe(finalize(() => this.loadingSubject.next(false)))
      .subscribe(data => {
        this.pageSubject.next(data.page);
        this.connectorDataSubject.next(data._embedded ? data._embedded.jmsConnections : []);
      });
  }
}
