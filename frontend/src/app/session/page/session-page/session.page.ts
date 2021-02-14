import { Component, OnInit, ViewChild, AfterViewInit, AfterContentInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { ConnectorView } from 'src/app/api/connector';
import { BookmarksComponent } from '../../component/bookmarks/bookmarks.component';
import { SubscriptionsHolder } from '@sterlp/ng-spring-boot-api';
import { JmsResource } from 'src/app/api/jms-session';

@Component({
  templateUrl: './session.page.html',
  styleUrls: ['./session.page.scss']
})
// tslint:disable: curly component-class-suffix
export class SessionPage implements OnInit, AfterContentInit, OnDestroy {
  id: number;
  private subs = new SubscriptionsHolder();

  loading = false;
  conData: ConnectorView;

  @ViewChild(BookmarksComponent) bookmarkComponent: BookmarksComponent;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: JmsSessionService) { }

  ngOnInit() {
  }
  ngAfterContentInit(): void {
    const s = this.route.params.subscribe(params => {
      if (this.id !== params.id) {
        this.id = params.id;
        this.handleId(params.id);
      }
    });
    this.subs.add(s);
  }
  ngOnDestroy(): void {
    this.subs.close();
  }

  doAddBookmark(v: JmsResource) {
    this.bookmarkComponent.doBookmark(v);
  }

  private handleId(id: any) {
    id = id * 1;
    if (isNaN(id)) {
      this.goBack();
    } else {
      if (!this.conData || this.conData.id !== id) {
        this.loading = true;
        const s = this.sessionService.openSession(id).subscribe(sessions => {
          const v = this.sessionService.getStoredSession(id, sessions);
          if ( v && !this.conData || (this.conData && v && this.conData.id !== v.id)) {
            this.conData = v;
          }
          this.loading = false;
        });
        this.subs.add(s);
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
