import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { SendJmsMessageCommand, JmsResultMessage, JmsHeaderRequestValues } from 'src/app/api/jms-session';
import { Observable } from 'rxjs';
import { ConnectorView } from 'src/app/api/connector';
import { transition, trigger, state, animate, style } from '@angular/animations';
import { SubscriptionsHolder } from '@sterlp/ng-spring-boot-api';

@Component({
  templateUrl: './jms-message.page.html',
  styleUrls: ['./jms-message.page.scss'],
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
// tslint:disable: curly no-console component-class-suffix
export class JmsMessagePage implements OnInit, OnDestroy {

  private subs = new SubscriptionsHolder();

  connector: ConnectorView;
  target: string;
  jmsMessage: string;
  loading$: Observable<boolean>;
  jmsHeader = {} as JmsHeaderRequestValues;

  receivedMessages: JmsResultMessage[] = [];

  constructor(private route: ActivatedRoute,
              private router: Router,
              private sessionService: JmsSessionService) { }

  ngOnInit() {
    const s = this.route.params.subscribe(params => {
      this.target = params.target;
      const id =  params.id * 1;
      console.info('JmsMessagePage', params, id, this.target, this.connector);
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
    this.subs.add(s);
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
