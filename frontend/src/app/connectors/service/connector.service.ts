import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, Subscription } from 'rxjs';
import { SupportedConnector, ConnectorData, ConnectorView } from 'src/app/api/connector';
import { ArrayUtils } from 'src/app/common/utils';
import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';
import { Page, Pageable, EMPTY_PAGE } from '@sterlp/ng-spring-boot-api';
import { ErrorDialogService } from 'src/app/common/error-dialog/error-dialog.service';


@Injectable({
  providedIn: 'root'
})
export class ConnectorService {
  supportedConnectors = new BehaviorSubject<SupportedConnector[]>([]);
  supportedConnectors$ = this.supportedConnectors.asObservable();

  constructor(private http: HttpClient, private errorDialog: ErrorDialogService) { }

  getSupported(): Observable<SupportedConnector[]> {
    if (this.supportedConnectors.value.length === 0) {
      this.reloadSupportedConnectors();
    }
    return this.supportedConnectors$;
  }
  reloadSupportedConnectors(): Observable<SupportedConnector[]> {
    const res = this.http.get<SupportedConnector[]>('api/connectors')
                         .pipe(catchError(this.errorDialog.showError<SupportedConnector[]>('Load Supported Connectors', [])));
    res.subscribe(result => this.supportedConnectors.next(result));
    return res;
  }

  save(data: ConnectorData): Observable<ConnectorData> {
    return this.http.post<ConnectorData>('api/jms/connections', data)
      .pipe(
        catchError(this.errorDialog.showError<ConnectorData>('Save JMS Connector', data))
      );
  }

  delete(id: number) {
    return this.http.delete(`api/jms/connections/${id}`)
      .pipe(
        catchError(this.errorDialog.showError<ConnectorData>('Delete JMS Connector', null))
      );
  }

  getConnectorWithConfig(id: number): Observable<ConnectorData> {
    return this.http.get<ConnectorData>(`api/jms/connections/${id}`)
      .pipe(
        catchError(this.errorDialog.showError<ConnectorData>('Load JMS Connector', null))
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
      catchError(this.errorDialog.showError<Page<ConnectorView>>('Load JMS Connectors', EMPTY_PAGE))
    );
  }
}

export class ConnertorViewDataSource implements DataSource<ConnectorView> {
    constructor(private $connector: ConnectorService) {}

    private connectorDataSubject = new BehaviorSubject<ConnectorView[]>([]);
    private pageSubject = new BehaviorSubject<Page<ConnectorView>>(EMPTY_PAGE);

    // tslint:disable: variable-name
    private _lastRequest: Subscription;
    private _loading = new BehaviorSubject<boolean>(false);
    public readonly loading$ = this._loading.asObservable();
    public page$ = this.pageSubject.asObservable();

    connect(collectionViewer: CollectionViewer): Observable<ConnectorView[] | readonly ConnectorView[]> {
        return this.connectorDataSubject.asObservable();
    }

    disconnect(collectionViewer: CollectionViewer): void {
        this.connectorDataSubject.complete();
        this.pageSubject.complete();
    }

    loadConnectorData(page: number = 0, size: number = 10): void {
        this._cancel();
        this._loading.next(true);
        this._lastRequest = this.$connector.listConnections(Pageable.of(page, size))
            .pipe(finalize(() => this._loading.next(false)))
            .subscribe(data => {
                if (data.content) {
                    this.$connector.getSupported().subscribe(s => {
                        // tslint:disable-next-line: forin
                        for (const ac of s) {
                            for (const c of data.content) {
                                if (ac.id === c.type) {
                                    c._typeName = ac.name;
                                }
                            }
                        }
                    });
                }
                this.pageSubject.next(data);
                this.connectorDataSubject.next(data.content ? data.content : []);
            }
        );
    }

    private _cancel() {
        if (this._lastRequest && this._lastRequest.unsubscribe) { // cancel any pending requests ...
            this._lastRequest.unsubscribe();
            this._lastRequest = null;
        }
    }
}
