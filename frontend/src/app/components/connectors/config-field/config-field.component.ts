import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { ConfigMetaData, ConfigType } from './../../../api/connector'

@Component({
  selector: 'app-config-field',
  templateUrl: './config-field.component.html',
  styleUrls: ['./config-field.component.scss']
})
export class ConfigFieldComponent implements OnInit {
  
  
  _value: String;
  @Input("meta-field") metaField: ConfigMetaData;
  @Input()
  get value(): String { return this._value;}
  set value(value: String) {    
    if (this._value != value) {
      this._value = value;
      this.valueChange.emit(this._value);
    }
  }
  @Output() valueChange: EventEmitter<String> = new EventEmitter();

  constructor() { }

  ngOnInit() {
    if (this.value == null && this.metaField.defaultValue != null) {
      // avoid ExpressionChangedAfterItHasBeenCheckedError: Expression has changed after it was checked.
      setTimeout(() => {
        if (this.value == null) this.value = this.metaField.defaultValue;
      }, 0);
    }
  }

  getType(): String {
    let result = "text";
    if (this.metaField.type == ConfigType.PASSWORD) result = 'password';
    else if (this.metaField.type == ConfigType.NUMBER) result = 'number';

    return result;
  }
}