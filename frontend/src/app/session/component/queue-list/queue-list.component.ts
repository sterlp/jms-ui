import { Component, OnInit, Input, ViewChild, AfterViewInit } from '@angular/core';
import { JmsResource, JmsResourceModel } from 'src/app/api/jms-session';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { JmsSessionService } from '../../service/session/jms-session.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-queue-list',
  templateUrl: './queue-list.component.html',
  styleUrls: ['./queue-list.component.scss']
})
// tslint:disable: curly variable-name
export class QueueListComponent implements OnInit, AfterViewInit {

    readonly columns = JmsResourceModel.COLUMNS;
    displayColumns = this.columns.map(c => c.id);

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;
    dataSource = new MatTableDataSource<JmsResource>([]);
    loading$: Observable<boolean>;

    @Input() connectorId: number;

    constructor(private sessionService: JmsSessionService) { }

    ngOnInit(): void {
        this.displayColumns.push('actions');
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.loading$ = this.sessionService.loading$;
    }
    ngAfterViewInit(): void {
        // avoid view update, so loading the stuff in the next tick ...
        setTimeout(() => this.loadQueues());
    }

    applyFilter(filterValue: string) {
        filterValue = filterValue.trim(); // Remove whitespace
        filterValue = filterValue.toLowerCase(); // Datasource defaults to lowercase matches
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage();
        }
    }
    // https://stackblitz.com/angular/dnbermjydavk?file=app%2Ftable-overview-example.ts
    loadQueues() {
        this.sessionService.getQueues(this.connectorId).subscribe(queues => {
            this.dataSource.disconnect();
            this.dataSource = new MatTableDataSource(queues);
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
            if (this.paginator) this.paginator.length = queues.length;

            const names = queues.map(q => q.name);
            this.sessionService.getDepths(this.connectorId, names).subscribe(depths => {
                if (depths != null) {
                    this.dataSource.data.forEach(e => {
                        const depth = depths[e.name];
                        e._depth = depth;
                    });
                    this.dataSource.data = this.dataSource.data;
                }
            });
        });
    }
}
