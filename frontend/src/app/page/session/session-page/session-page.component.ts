import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { switchMap, filter, withLatestFrom, map } from 'rxjs/operators';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { JmsSessionService } from 'src/app/components/jms-sessions/jms-session.service';
import { ConnectorService } from 'src/app/components/connectors/connector.service';
import { ConnectorData } from 'src/app/api/connector';
import { Observable } from 'rxjs';
import { JmsResource } from 'src/app/api/jms-session';
import { MatTableDataSource, MatPaginator, MatSort } from '@angular/material';

@Component({
  selector: 'app-session-page',
  templateUrl: './session-page.component.html',
  styleUrls: ['./session-page.component.scss']
})
export class SessionPageComponent implements OnInit, AfterViewInit {

  conData: ConnectorData;
  dataSource = new MatTableDataSource<JmsResource>([]);

  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: false}) sort: MatSort;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: JmsSessionService,
    private connectorService: ConnectorService) { }

  ngOnInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
  ngAfterViewInit(): void {
    this.route.params.subscribe(params => {
      const id = parseInt(params.id);
      this.conData = this.sessionService.sessions$.value.find(d => d.id == id);
      if (this.conData) {
        if (this.dataSource.data.length === 0) {
          this.loadQueues();
        }
      } else {
        this.router.navigate(['/connectors']);
      }
    });
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
    this.sessionService.getQueues(this.conData.id).subscribe(queues => {
      this.dataSource = new MatTableDataSource(queues);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      if (this.paginator) this.paginator.length = queues.length;
    });
  }
}
