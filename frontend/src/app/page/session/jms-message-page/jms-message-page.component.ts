import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JmsSessionService } from 'src/app/components/jms-sessions/jms-session.service';
import { ConnectorService } from 'src/app/components/connectors/connector.service';
import { SendJmsMessageCommand, JmsResultMessage, JmsHeaderRequestValues } from 'src/app/api/jms-session';
import { ArrayUtils } from 'src/app/common/utils';

@Component({
  selector: 'app-jms-message-page',
  templateUrl: './jms-message-page.component.html',
  styleUrls: ['./jms-message-page.component.scss']
})
export class JmsMessagePageComponent implements OnInit, AfterViewInit {

  connector: number;
  target: string;
  jmsMessage: string;
  loading$;
  jmsHeader = {} as JmsHeaderRequestValues;

  receivedMessages: JmsResultMessage[] = [];

  constructor(private route: ActivatedRoute,
    private router: Router,
    private sessionService: JmsSessionService,
    private connectorService: ConnectorService) { }

  ngOnInit() {
    this.loading$ = this.sessionService.loading$;
  }

  ngAfterViewInit(): void {
    this.route.params.subscribe(params => {
      this.connector = params.id * 1;
      this.target = params.target;
      console.info('JmsMessagePageComponent', this.connector, this.target);
    });
    const m = new Map();
    m.set('fff', 1);
    console.info( ArrayUtils.forEach({foo: 1}) );
    console.info( ArrayUtils.forEach(m) );
  }

  doSend() {
    const body: SendJmsMessageCommand = {
      body: this.jmsMessage,
      header: this.jmsHeader
    };
    this.sessionService.sendJmsMessage(this.connector, this.target, body)
      .subscribe(r => {
        console.info("send ...", r);
      });
  }
  doListen() {
    const startTime = new Date();
    this.sessionService.receiveJmsMessage(this.connector, this.target)
      .subscribe(r => {
        console.info("receive ...", r, r.header.JMSDestination);
        if (r.header || r.body) {
          r._time = new Date().getDate() - startTime.getDate();
          this.receivedMessages.push(r);
        }
      });
  }
}
