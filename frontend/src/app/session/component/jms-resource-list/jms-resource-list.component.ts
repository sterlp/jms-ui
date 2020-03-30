import { Component, OnInit, ViewChild, Input, AfterViewInit, Output, EventEmitter } from '@angular/core';
import { JmsResourceModel, JmsResource, JmsResourceType } from 'src/app/api/jms-session';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { LoadingHelper } from 'src/app/common/loading/loading.helper';
import { JmsSessionService } from '../../service/session/jms-session.service';
import { ErrorDialogService } from 'src/app/common/error-dialog/error-dialog.service';
import { ThrowStmt } from '@angular/compiler';

@Component({
  selector: 'app-jms-resource-list',
  templateUrl: './jms-resource-list.component.html',
  styleUrls: ['./jms-resource-list.component.scss']
})
// tslint:disable: curly variable-name
export class JmsResourceListComponent implements OnInit, AfterViewInit {

    readonly columns = JmsResourceModel.COLUMNS;
    displayColumns = this.columns.map(c => c.id);

    @Output() addBookmark: EventEmitter<any> = new EventEmitter();
    @Input() resourceType: JmsResourceType | string = JmsResourceType.QUEUE;
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;
    dataSource = new MatTableDataSource<JmsResource>([]);

    private loading = new LoadingHelper();
    loading$ = this.loading.loading$;
    error: any;

    @Input() connectorId: number;

    constructor(private sessionService: JmsSessionService, private errorService: ErrorDialogService) { }

    ngOnInit(): void {
        this.displayColumns.push('actions');
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;

        console.info('JmsResourceListComponent for', this.resourceType, this.resourceType === JmsResourceType.QUEUE, this.resourceType === JmsResourceType.TOPIC);
        if (this.resourceType === JmsResourceType.TOPIC) {
            this.displayColumns = this.displayColumns.filter(c => c !== 'depth');
        }
    }
    ngAfterViewInit(): void {
        // avoid view update, so loading the stuff in the next tick ...
        setTimeout(() => this.load());
    }

    applyFilter(filterValue: string) {
        filterValue = filterValue.trim(); // Remove whitespace
        filterValue = filterValue.toLowerCase(); // Datasource defaults to lowercase matches
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage();
        }
    }
    doBookmark(element: JmsResource) {
        this.addBookmark.emit(element);
    }
    // https://stackblitz.com/angular/dnbermjydavk?file=app%2Ftable-overview-example.ts
    load() {
        this.error = false;
        this.loading.loading();
        if (this.resourceType === JmsResourceType.TOPIC) {
            this.sessionService.listTopics(this.connectorId).subscribe(
                resources => this.updateView(resources),
                e => this.error = e.error || e,
                () => this.loading.done());
        } else {
            this.sessionService.listQueues(this.connectorId).subscribe(
                resources => this.updateView(resources),
                e => this.error = e.error || e,
                () => this.loading.done());
        }
    }

    private updateView(resources: JmsResource[]) {
        this.dataSource.disconnect();
        this.dataSource = new MatTableDataSource(resources);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        if (this.paginator) this.paginator.length = resources.length;

        const qNames = resources.filter(r => r.type === 'QUEUE' || r.type === JmsResourceType.QUEUE) .map(q => q.name);
        if (qNames.length > 0) {
            this.loading.loading();
            this.sessionService.getDepths(this.connectorId, qNames).subscribe(depths => {
                if (depths != null) {
                    this.dataSource.data.forEach(e => {
                        const depth = depths[e.name];
                        e._depth = depth;
                    });
                    this.dataSource.data = this.dataSource.data;
                }
            },
            e => this.error = e.error || e,
            //this.errorService.showError('Failed to Queue depths', null),
            () => this.loading.done());
        }
    }

}
