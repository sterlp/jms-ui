import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { ConnectorView } from 'src/app/api/connector';
import { JmsResource, SendJmsMessageCommand, JmsResultMessage } from 'src/app/api/jms-session';
import { BehaviorSubject } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Page } from '@sterlp/ng-spring-boot-api';

// /api/jms/sessions
@Injectable({
  providedIn: 'root'
})
export class JmsSessionService implements OnDestroy {

    private readonly listUrl = '/api/sessions';
    public sessions$ = new BehaviorSubject<ConnectorView[]>([]);

    constructor(private http: HttpClient) {}

    ngOnDestroy(): void {
        this.sessions$.complete();
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
        return this.http.post<Page<ConnectorView>>(`${this.listUrl}/${connectorId}`, null)
            .pipe(
                map(s => {
                    if (s && s.content) {
                        this.sessions$.next(s.content);
                        return s.content;
                    }
                    return [];
                })
            );
    }

    closeSession(connectorId: number): Observable<ConnectorView[]> {
        return this.http.delete<Page<ConnectorView>>(`${this.listUrl}/${connectorId}`)
            .pipe(
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
        return this.http.post<void>(`${this.listUrl}/${connectorId}/message/${target}`, body);
    }

    receiveJmsMessage(connectorId: number, target: string): Observable<JmsResultMessage> {
        return this.http.get<JmsResultMessage>(`${this.listUrl}/${connectorId}/message/${target}`);
    }

    listResources(connectorId: number): Observable<JmsResource[]> {
        return this.http.get<JmsResource[]>(`${this.listUrl}/${connectorId}/resources`);
    }

    /**
     * Returns a object map of the current queue length. Call this method only with queues.
     */
    getDepths(connectorId: number, queues: string[]): Observable<object> {
        return this.http.post<object>(`${this.listUrl}/${connectorId}/queue/depths`, queues);
    }
}
