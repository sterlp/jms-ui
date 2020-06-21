import { Component, OnInit, Inject, Input } from '@angular/core';
import { JmsSessionService } from '../../service/session/jms-session.service';
import { JmsResourceType, JmsResource } from 'src/app/api/jms-session';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, BehaviorSubject, Subject } from 'rxjs';

export interface JmsResourceDetailData {
    connectorId: number;
    destination: string;
    type: JmsResourceType;
}

interface CacheEntry {
    date: Date;
    data: any;
}

@Component({
  selector: 'app-jms-resource-details',
  templateUrl: './jms-resource-details.component.html',
  styleUrls: ['./jms-resource-details.component.scss']
})
export class JmsResourceDetailsComponent implements OnInit {
    private static CACHE = new Map<string, CacheEntry>();

    @Input() connectorId: number;
    @Input() resource: JmsResource;

    error: any;
    private readonly isLoading = new BehaviorSubject(false);
    loading$ = this.isLoading.asObservable();
    queueData: CacheEntry;
    constructor(private sessionService: JmsSessionService) { }

    ngOnInit(): void {
        if (this.resource) {
            this.queueData = JmsResourceDetailsComponent.CACHE.get(this.connectorId + this.resource.name);
            if (this.queueData == null) {
                this.doLoad();
            }
        }
    }

    doLoad() {
        this.error = null;
        this.isLoading.next(true);
        if (this.resource.type === JmsResourceType.QUEUE) {
                this.sessionService.getQueueInfo(this.connectorId, this.resource.name).subscribe(queueData => {
                    this.queueData = {date: new Date(), data: queueData};
                    JmsResourceDetailsComponent.CACHE.set(this.connectorId + this.resource.name, this.queueData);
                },
                e => this.error = e.error || e,
                () => this.isLoading.next(false)
            );
        } else {
            this.sessionService.getTopicInfo(this.connectorId, this.resource.name).subscribe(queueData => {
                this.queueData = {date: new Date(), data: queueData};
                JmsResourceDetailsComponent.CACHE.set(this.connectorId + this.resource.name, this.queueData);
            },
            e => this.error = e.error || e,
            () => this.isLoading.next(false));
        }
    }
}
