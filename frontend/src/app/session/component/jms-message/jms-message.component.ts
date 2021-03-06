import { Component, OnInit, Input } from '@angular/core';
import { JmsMessage, JmsHeader } from 'src/app/api/jms-session';
import { ArrayUtils } from 'src/app/common/utils';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-jms-message',
  templateUrl: './jms-message.component.html',
  styleUrls: ['./jms-message.component.scss']
})
export class JmsMessageComponent implements OnInit {

  @Input('jms-message') jmsMessage: JmsMessage<JmsHeader>;
  @Input() key ? = 'default';
  constructor(private datePipe: DatePipe) { }

  ngOnInit() {
  }

  getHeaders(): Map<string, any> {
    const result = new Map();
    // tslint:disable: curly
    if (this.jmsMessage && this.jmsMessage.header) {
      for (const [key, value] of Object.entries(this.jmsMessage.header)) {
        if (value && key !== 'properties') result.set(key, value);
      }
      for (const [key, value] of ArrayUtils.forEach(this.jmsMessage.header.properties)) {
        result.set(key, value);
      }
    }
    return result;
  }

  format(type: string, val: any): any {
    if (!val) return val;

    if (type === 'JMSDeliveryTime' || type === 'JMSTimestamp') {
      return this.datePipe.transform(val, 'medium');
    } else {
      return val;
    }
  }
}
