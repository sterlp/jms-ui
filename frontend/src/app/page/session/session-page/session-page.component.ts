import { Component, OnInit, ViewChild, AfterViewInit, AfterContentInit, OnDestroy } from '@angular/core';
import { switchMap, filter, withLatestFrom, map } from 'rxjs/operators';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { JmsSessionService } from 'src/app/page/session/jms-session.service';
import { ConnectorService } from 'src/app/components/connectors/connector.service';
import { ConnectorData, ConnectorView } from 'src/app/api/connector';
import { Observable } from 'rxjs';
import { JmsResource } from 'src/app/api/jms-session';
import { MatTableDataSource, MatPaginator, MatSort } from '@angular/material';
import { SubscriptionsHolder } from 'projects/ng-spring-boot-api/src/public-api';
import { BookmarksComponent } from 'src/app/components/bookmarks/list/bookmarks.component';

@Component({
  selector: 'app-session-page',
  templateUrl: './session-page.component.html',
  styleUrls: ['./session-page.component.scss']
})
// tslint:disable: curly
export class SessionPageComponent implements OnInit, AfterContentInit, OnDestroy {
  id: number;
  private subs = new SubscriptionsHolder();

  loading$: Observable<boolean>;
  conData: ConnectorView;
  dataSource = new MatTableDataSource<JmsResource>([]);

  @ViewChild(BookmarksComponent, {static: false}) bookmarkComponent: BookmarksComponent;
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
    this.loading$ = this.sessionService.loading$;
  }
  ngAfterContentInit(): void {
    const s = this.route.params.subscribe(params => {
      if (this.id !== params.id) {
        this.id = params.id;
        this.handleId(params.id);
      }
    });
    this.subs.addAny(s);
  }
  ngOnDestroy(): void {
    this.subs.close();
  }

  private handleId(id: any) {
    id = id * 1;
    if (isNaN(id)) {
      this.goBack();
    } else {
      if (!this.conData || this.conData.id !== id) {
        const s = this.sessionService.openSession(id).subscribe(sessions => {
          const v = this.sessionService.getStoredSession(id, sessions);
          if ( v && !this.conData || (this.conData && v && this.conData.id !== v.id)) {
            this.conData = v;
            this.loadQueues();
          }
        });
        this.subs.addAny(s);
      }
    }
  }

  goBack() {
    this.router.navigate(['/jms-connectors']);
  }

  doDisconnect() {
    this.sessionService.closeSession(this.id).subscribe(v => this.goBack());
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
      this.dataSource.disconnect();
      this.dataSource = new MatTableDataSource(queues);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      if (this.paginator) this.paginator.length = queues.length;
    });
  }
}
