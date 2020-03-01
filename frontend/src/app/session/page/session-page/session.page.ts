import { Component, OnInit, ViewChild, AfterViewInit, AfterContentInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { ConnectorView } from 'src/app/api/connector';
import { Observable } from 'rxjs';
import { JmsResource } from 'src/app/api/jms-session';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { SubscriptionsHolder } from 'projects/ng-spring-boot-api/src/public-api';
import { BookmarksComponent } from '../../component/bookmarks/bookmarks.component';

@Component({
  templateUrl: './session.page.html',
  styleUrls: ['./session.page.scss']
})
// tslint:disable: curly component-class-suffix
export class SessionPage implements OnInit, AfterContentInit, OnDestroy {
  id: number;
  private subs = new SubscriptionsHolder();

  loading$: Observable<boolean>;
  conData: ConnectorView;

  @ViewChild(BookmarksComponent) bookmarkComponent: BookmarksComponent;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: JmsSessionService) { }

  ngOnInit() {
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
}
