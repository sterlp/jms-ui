import { Component, OnInit, ViewChild } from '@angular/core';
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
export class SessionPageComponent implements OnInit {
  conData: ConnectorData;
  dataSource: MatTableDataSource<JmsResource> = new MatTableDataSource([]);

  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: false}) sort: MatSort;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: JmsSessionService,
    private connectorService: ConnectorService) { }

  ngOnInit() {
    this.sessionService.sessions$.subscribe(sessions => {
      const id = parseInt(this.route.snapshot.paramMap.get('id'));
      this.conData = sessions.find(d => d.id == id);
      if (this.conData) {
        if (this.dataSource.data.length === 0) {
          this.loadQueues();
        } else {
          this.connectorService.getConnectorWithConfig(id).subscribe(v => {
            this.sessionService.openSession(v);
            this.loadQueues();
          });
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
      this.paginator.length = queues.length;
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }
}
