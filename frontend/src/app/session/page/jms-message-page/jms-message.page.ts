import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { SendJmsMessageCommand, JmsResultMessage, JmsHeaderRequestValues, JmsResourceType } from 'src/app/api/jms-session';
import { Observable } from 'rxjs';
import { ConnectorView } from 'src/app/api/connector';
import { transition, trigger, state, animate, style } from '@angular/animations';
import { SubscriptionsHolder } from '@sterlp/ng-spring-boot-api';
import { DecimalPipe } from '@angular/common';

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
  targetType: JmsResourceType;
  jmsMessage: string;
  loading$: Observable<boolean>;
  jmsHeader = {} as JmsHeaderRequestValues;

  headerMessage: any;

  receivedMessages: JmsResultMessage[] = [];

  constructor(private route: ActivatedRoute,
              private router: Router,
              private sessionService: JmsSessionService,
              private numberPipe: DecimalPipe) { }

  ngOnInit() {
    const s = this.route.params.subscribe(params => {
      this.target = params.target;
      this.targetType = params.type === 'TOPIC' ? JmsResourceType.TOPIC : JmsResourceType.QUEUE;
      const id =  params.id * 1;
      // console.info('JmsMessagePage', params, id, this.target, this.targetType, this.connector);
      if (isNaN(id)) {
        this.doClose();
      } else {
        if (!this.connector || this.connector.id !== id) {
          this.sessionService.openSession(id).subscribe(sessions => {
            this.connector = this.sessionService.getStoredSession(id, sessions);
            if (!this.connector) this.doClose();
            else this.sessionService.markAsOpen(id, {name: this.target, type: this.targetType});
          });
        }
      }
    });
    this.subs.add(s);
  }

  ngOnDestroy(): void {
    this.subs.close();
  }

  doClose() {
    this.sessionService.markAsClosed(this.connector.id, {name: this.target, type: this.targetType});
    this.router.navigate(['/sessions', this.connector.id]);
  }

  doSend() {
    this.clearMessage();
    const startTime = new Date();
    const body: SendJmsMessageCommand = {
      body: this.jmsMessage || '',
      header: this.jmsHeader,
      destination: this.target,
      destinationType: this.targetType
    };
    this.sessionService.sendJmsMessage(this.connector.id, this.target, body)
      .subscribe(r => {
        const time = new Date().getTime() - startTime.getTime();
        // console.info('message:', body, 'send in:', time);
        this.showMessage('Message successsfully send in ' + this.numberPipe.transform(time) + 'ms.');
      },
      e => {
          this.headerMessage = e.error;
          this.headerMessage.style = 'alert-danger';
      }
    );
  }
  doReceive() {
    this.clearMessage();
    const startTime = new Date();
    this.sessionService.receiveJmsMessage(this.connector.id, this.target, this.targetType)
      .subscribe(r => {
        const time = new Date().getTime() - startTime.getTime();
        if (r && (r.header || r.body)) {
          r._time = time;
          this.receivedMessages.unshift(r);
        } else {
          this.headerMessage = 'No message recevied in ' + this.numberPipe.transform(time) + 'ms from ' + this.target + '.';
        }
        console.info('received message:', r, 'time:', time);
      },
      e => {
          this.headerMessage = e.error;
          this.headerMessage.style = 'alert-danger';
      }
    );
  }
  doClear(): void {
    this.receivedMessages = [];
  }

  private showMessage(message: string, style: 'alert-success' | 'alert-primary' | 'alert-danger' | 'alert-warning' = 'alert-success') {
    this.headerMessage = {
        header: this.target + ':',
        message, style
    };
  }
  private clearMessage() {
      this.headerMessage = null;
  }
}
