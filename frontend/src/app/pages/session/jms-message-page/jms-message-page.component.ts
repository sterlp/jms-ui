import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JmsSessionService } from 'src/app/components/jms-sessions/jms-session.service';
import { ConnectorService } from 'src/app/components/connectors/connector.service';

@Component({
  selector: 'app-jms-message-page',
  templateUrl: './jms-message-page.component.html',
  styleUrls: ['./jms-message-page.component.scss']
})
export class JmsMessagePageComponent implements OnInit, AfterViewInit {

  connector: number;
  target: string;
  constructor(private route: ActivatedRoute,
    private router: Router,
    private sessionService: JmsSessionService,
    private connectorService: ConnectorService) { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    this.route.params.subscribe(params => {
      this.connector = parseInt(params.id);
      this.target = params.target;
      console.info('JmsMessagePageComponent', this.connector, this.target);
    });
  }

}
