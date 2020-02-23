import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { JmsHeaderRequestValues } from 'src/app/api/jms-session';


interface HeaderEntry {
  key?: string;
  value?: any;
}

@Component({
  selector: 'app-jms-headers',
  templateUrl: './jms-headers.component.html',
  styleUrls: ['./jms-headers.component.scss']
})
// tslint:disable: curly
export class JmsHeadersComponent implements OnInit {

  constructor() { }

  oldKey: string;

  private internalHeader: JmsHeaderRequestValues;
  headers: Array<HeaderEntry> = [];
  readonly jmsOptions: string[] = ['JMSCorrelationID', 'JMSDeliveryMode', 'JMSExpiration', 'JMSMessageID', 'JMSPriority',
                                   'JMSTimestamp', 'JMSType', 'MQMDS_PRIORITY'];
  readonly jmsNumberOptions: string[] = ['JMSDeliveryMode', 'JMSExpiration', 'JMSPriority', 'JMSTimestamp', 'MQMDS_PRIORITY'];

  private readonly jmsFields = ['JMSType', 'JMSDeliveryMode', 'JMSPriority', 'JMSTimestamp',
                                'JMSExpiration', 'JMSMessageID', 'JMSCorrelationID'];

  @Output() headersChange = new EventEmitter<JmsHeaderRequestValues>();
  @Input()
  get header(): JmsHeaderRequestValues {
    return this.internalHeader;
  }
  set header(jmsHeaders: JmsHeaderRequestValues) {
    this.internalHeader = jmsHeaders;
    if (!this.internalHeader) {
      this.internalHeader = {properties: new Map()} as JmsHeaderRequestValues;
    } else {
      this.headers.length = 0;
      for (const o of this.jmsOptions) {
        if (jmsHeaders[o]) this.headers.push({key: o, value: jmsHeaders[o]});
      }
      if (!jmsHeaders.properties) {
        this.internalHeader.properties = new Map();
      } else if (jmsHeaders.properties) {
        if (jmsHeaders.properties.forEach) jmsHeaders.properties.forEach((key, value) => this.headers.push({key, value}));
        else {
          for (const [key, value] of Object.entries(jmsHeaders.properties)) {
            this.headers.push({key, value});
          }
        }
      }
    }
  }

  ngOnInit() {
    if (!this.internalHeader) this.internalHeader = {properties: new Map()} as JmsHeaderRequestValues;
    if (this.headers.length === 0) this.addHeader();
  }
  addHeader() {
    this.headers.push({key: '', value: null});
  }
  removeHeader(header: HeaderEntry) {
    this.headers = this.headers.filter(h => h !== header);
    if (this.headers.length === 0) this.addHeader();
  }
  dataChange(key: string, value: string | number, oldKey?: any) {
    // is it a field?
    if (this.jmsFields.indexOf(key) > -1) {
        this.internalHeader[key] = value;
    } else {
        this.internalHeader.properties[key] = value;
    }
    if (oldKey && oldKey !== key) {
        if (this.jmsFields.indexOf(oldKey) > -1) {
            this.internalHeader[oldKey] = null;
        } else {
            delete this.internalHeader.properties[oldKey];
        }
    }
    this.headersChange.emit(this.internalHeader);
  }

  asNumber(value: any): string | number {
    if (!value || value.length === 0) return value;
    try {
      const result = value * 1;
      if (isNaN(result)) return value;
      else return result;
    } catch (e) {
      return value;
    }
  }
}
