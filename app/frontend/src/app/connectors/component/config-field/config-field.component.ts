import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { ConfigMetaData, ConfigType } from '../../../api/connector'

@Component({
  selector: 'app-config-field',
  templateUrl: './config-field.component.html',
  styleUrls: ['./config-field.component.scss']
})
// tslint:disable: curly
export class ConfigFieldComponent implements OnInit {

    _value: string | boolean | number;
    @Input("meta-field") metaField: ConfigMetaData;
    @Input()
    get value(): string | boolean | number {
        return this._value;
    }
    set value(value: string | boolean | number) {
        if (this._value !== value) {

            if (this.metaField && this.metaField.type === ConfigType.BOOLEAN) {
                value = value === 'true' || value === true ? true : false;
            }
            if (this.value !== value) {
                this._value = value;
                this.valueChange.emit(this._value);
            }
        }
    }
    @Output() valueChange: EventEmitter<string | boolean | number> = new EventEmitter();

    constructor() { }

  ngOnInit() {
    if (this.value == null && this.metaField.defaultValue != null) {
      // avoid ExpressionChangedAfterItHasBeenCheckedError: Expression has changed after it was checked.
      setTimeout(() => {
        if (this.value == null) this.value = this.metaField.defaultValue;
      }, 0);
    }
  }

  getType(): string {
    let result = 'text';
    if (this.metaField.type === ConfigType.PASSWORD) result = 'password';
    else if (this.metaField.type === ConfigType.NUMBER) result = 'number';
    else if (this.metaField.type === ConfigType.BOOLEAN) result = 'checkbox';

    return result;
  }
}
