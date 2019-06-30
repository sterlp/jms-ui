import { Component, OnInit, OnDestroy } from '@angular/core';
import { JmsSessionService } from 'src/app/components/jms-sessions/jms-session.service';
import { Subscriptions } from 'src/app/common/utils';
import { ConnectorData } from 'src/app/api/connector';


@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit, OnDestroy {
  subs = new Subscriptions();
  sessions: ConnectorData[] = [];
  constructor(private jmsSession: JmsSessionService) { }

  ngOnInit() {
    this.subs.manage(
      this.jmsSession.sessions$.subscribe(v => { this.sessions = v; }));
  }
  ngOnDestroy(): void {
    this.subs.ngOnDestroy();
  }
}
