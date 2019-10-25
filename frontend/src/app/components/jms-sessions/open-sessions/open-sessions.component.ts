import { Component, OnInit } from '@angular/core';
import { JmsSessionService } from '../jms-session.service';
import { ConnectorView } from 'src/app/api/connector';

@Component({
  selector: 'app-open-sessions',
  templateUrl: './open-sessions.component.html',
  styleUrls: ['./open-sessions.component.scss']
})
export class OpenSessionsComponent implements OnInit {

  constructor(private sessionsService: JmsSessionService) { }
  sessions: ConnectorView[] = [];
  ngOnInit() {
    this.sessionsService.sessions$.subscribe(s => this.sessions = s);
  }
}
