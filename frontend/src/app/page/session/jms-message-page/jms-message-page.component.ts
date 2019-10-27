import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JmsSessionService } from 'src/app/page/session/jms-session.service';
import { ConnectorService } from 'src/app/components/connectors/connector.service';
import { SendJmsMessageCommand, JmsResultMessage, JmsHeaderRequestValues } from 'src/app/api/jms-session';
import { ArrayUtils } from 'src/app/common/utils';
import { Observable } from 'rxjs';
import { ConnectorView } from 'src/app/api/connector';
import { SubscriptionsHolder } from 'projects/ng-spring-boot-api/src/public-api';
import { transition, trigger, state, animate, style } from '@angular/animations';

@Component({
  selector: 'app-jms-message-page',
  templateUrl: './jms-message-page.component.html',
  styleUrls: ['./jms-message-page.component.scss'],
  animations: [
    trigger('newMessage', [
      transition(':enter', [
        style({ backgroundColor: 'var(--orange)' }),
        animate(500, style({ backgroundColor: 'var(--white)' })),
      ])
    ])
    /*
    trigger('newMessage', [
        state('active', style({
            backgroundColor: 'var(--orange)',
        })),
        state('inactive', style({
            backgroundColor: 'var(--white)',
        })),
        transition('active => inactive', animate(500))
    ])
    */
  ]
})
// tslint:disable: curly no-console
export class JmsMessagePageComponent implements OnInit, OnDestroy {

  private subs = new SubscriptionsHolder();

  connector: ConnectorView;
  target: string;
  jmsMessage: string;
  loading$: Observable<boolean>;
  jmsHeader = {} as JmsHeaderRequestValues;

  receivedMessages: JmsResultMessage[] = [];

  constructor(private route: ActivatedRoute,
              private router: Router,
              private sessionService: JmsSessionService,
              private connectorService: ConnectorService) { }

  ngOnInit() {
    this.loading$ = this.sessionService.loading$;
    const s = this.route.params.subscribe(params => {
      this.target = params.target;
      const id =  params.id * 1;
      console.info('JmsMessagePageComponent', params, id, this.target, this.connector);
      if (isNaN(id)) {
        this.goBack();
      } else {
        if (!this.connector || this.connector.id !== id) {
          this.sessionService.openSession(id).subscribe(sessions => {
            this.connector = this.sessionService.getStoredSession(id, sessions);
            if (!this.connector) this.goBack();
          });
        }
      }
    });
    this.subs.addAny(s);
  }

  ngOnDestroy(): void {
    this.subs.close();
  }

  goBack() {
    this.router.navigate(['/jms-connectors']);
  }

  doSend() {
    const body: SendJmsMessageCommand = {
      body: this.jmsMessage || '',
      header: this.jmsHeader
    };
    this.sessionService.sendJmsMessage(this.connector.id, this.target, body)
      .subscribe(r => {
        console.info("send ...", r);
      }
    );
  }
  doListen() {
    const startTime = new Date();
    this.sessionService.receiveJmsMessage(this.connector.id, this.target)
      .subscribe(r => {
        console.info("receive ...", r);
        if (r && (r.header || r.body)) {
          r._time = new Date().getDate() - startTime.getDate();
          this.receivedMessages.unshift(r);

        } else {
          // TODO no message ...
        }
      }
    );
  }
  doClear(): void {
    this.receivedMessages = [];
  }
}
