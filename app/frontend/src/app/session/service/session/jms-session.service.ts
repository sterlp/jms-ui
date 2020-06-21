import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { ConnectorView } from 'src/app/api/connector';
import { JmsResource, SendJmsMessageCommand, JmsResultMessage, JmsResourceType, isSameJmsResource } from 'src/app/api/jms-session';
import { BehaviorSubject } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Page } from '@sterlp/ng-spring-boot-api';

// /api/jms/sessions
@Injectable({
  providedIn: 'root'
})
// tslint:disable: curly
export class JmsSessionService implements OnDestroy {

    private readonly listUrl = '/api/sessions';
    private openDestinations = new Map<number, JmsResource[]>();
    get openResources() {
        return this.openDestinations;
    }

    public sessions$ = new BehaviorSubject<ConnectorView[]>([]);

    constructor(private http: HttpClient) {}

    ngOnDestroy(): void {
        this.sessions$.complete();
    }

    getStoredSession(connectorId: number, connectors?: ConnectorView[]): ConnectorView {
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
        this.openDestinations.delete(connectorId);
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

    markAsOpen(connectorId: number, resource: JmsResource) {
        let open = this.openDestinations.get(connectorId);
        if (open == null) {
            open = [resource];
            this.openDestinations.set(connectorId, open);
        } else {
            if (!open.find(r => isSameJmsResource(r, resource))) {
                open.push(resource);
            }
        }

    }
    markAsClosed(connectorId: number, resource: JmsResource) {
        const open = this.openDestinations.get(connectorId);
        if (open != null) {
            this.openDestinations.set(connectorId, open.filter(r => !isSameJmsResource(r, resource)));
        }
    }

    sendJmsMessage(connectorId: number, target: string, body: SendJmsMessageCommand): Observable<void> {
        return this.http.post<void>(`${this.listUrl}/${connectorId}/message/${target}`, body);
    }

    receiveJmsMessage(connectorId: number, target: string, type = JmsResourceType.QUEUE): Observable<JmsResultMessage> {
        const params = new HttpParams().set('type', type);
        return this.http.get<JmsResultMessage>(`${this.listUrl}/${connectorId}/message/${target}`, {params});
    }

    listQueues(connectorId: number): Observable<JmsResource[]> {
        return this.http.get<JmsResource[]>(`${this.listUrl}/${connectorId}/queues`);
    }
    listTopics(connectorId: number): Observable<JmsResource[]> {
        return this.http.get<JmsResource[]>(`${this.listUrl}/${connectorId}/topics`);
    }

    /**
     * Returns a object map of the current queue length. Call this method only with queues.
     */
    getDepths(connectorId: number, queues: string[]): Observable<object> {
        return this.http.post<object>(`${this.listUrl}/${connectorId}/queue/depths`, queues);
    }

    getQueueInfo(connectorId: number, name: string): Observable<object> {
        return this.http.get<object>(`${this.listUrl}/${connectorId}/queues/${name}/info`);
    }
    getTopicInfo(connectorId: number, name: string): Observable<object> {
        return this.http.get<object>(`${this.listUrl}/${connectorId}/topics/${name}/info`);
    }
}
