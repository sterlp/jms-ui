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
  private internalHeader: JmsHeaderRequestValues;
  headers: Array<HeaderEntry> = [];
  jmsOptions: string[] = ['JMSCorrelationID', 'JMSDeliveryMode', 'JMSExpiration', 'JMSMessageID', 'JMSPriority', 
                          'JMSTimestamp', 'JMSType', 'MQMDS_PRIORITY'];
  private jmsNumberOptions: string[] = ['JMSDeliveryMode', 'JMSExpiration', 'JMSPriority', 'JMSTimestamp', 'MQMDS_PRIORITY'];

  @Output() headersChange = new EventEmitter<JmsHeaderRequestValues>();
  @Input()
  get header(): JmsHeaderRequestValues {
    return this.internalHeader;
  }
  set header(value: JmsHeaderRequestValues) {
    this.internalHeader = value;
    if (!this.internalHeader) {
      this.internalHeader = {properties: new Map()} as JmsHeaderRequestValues;
    } else {
      this.headers.length = 0;
      for (const o of this.jmsOptions) {
        if (value[o]) this.headers.push({key: o, value: value[o]});
      }
      if (!value.properties) {
        this.internalHeader.properties = new Map();
      } else if (value.properties) {
        if (value.properties.forEach) value.properties.forEach((k, v) => this.headers.push({key: k, value: v}));
        else {
          for (let [k, v] of Object.entries(value.properties)) {
            this.headers.push({key: k, value: v});
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
  dataChange() {
    let changed = false;
    this.headers.forEach(entry => {
      if (entry.key && entry.key.length > 0) { // only if a key is already set
        if (this.jmsNumberOptions.indexOf(entry.key) > -1) {
          if (this.internalHeader[entry.key] !== entry.value) {
            this.internalHeader[entry.key] = this.asNumber(entry.value);
            changed = true;
          }
        } else if (this.jmsOptions.indexOf(entry.key) > -1) {
          if (this.internalHeader[entry.key] !== entry.value) {
            this.internalHeader[entry.key] = entry.value;
            changed = true;
          }
        } else {
          const oldval = this.internalHeader.properties.get(entry.key);
          if (oldval !== entry.value) {
            this.internalHeader.properties.set(entry.key, entry.value);
            changed = true;
          }
        }
      }
    });
    if (changed) {
      console.debug('JmsHeadersComponent: data change-->', this.internalHeader);
      this.headersChange.emit(this.internalHeader);
    }
  }

  asNumber(value): string | number {
    if (!value || value.length === 0) return value;
    try {
      const result = parseInt(value);
      if (isNaN(result)) return value;
      else return result;
    } catch (e) {
      return value;
    }
  }
}
