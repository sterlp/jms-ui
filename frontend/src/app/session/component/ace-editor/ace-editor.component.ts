import { Component, OnInit, Input, EventEmitter, Output, ViewChild, AfterViewInit, AfterContentInit, AfterViewChecked } from '@angular/core';
import { AceConfigInterface, AceComponent, AceDirective } from 'ngx-ace-wrapper';
//declare var pd: any;
import * as pd from '@sterlp/pretty-data';
import 'brace';
import 'brace/mode/text';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/theme/github';
import 'brace/theme/twilight';
import 'brace/theme/eclipse';
import 'brace/theme/xcode';

@Component({
  selector: 'app-ace-editor',
  templateUrl: './ace-editor.component.html',
  styleUrls: ['./ace-editor.component.scss']
})
export class AceEditorComponent implements OnInit, AfterContentInit, AfterViewChecked {
  @ViewChild(AceComponent) componentRef?: AceComponent;
  @ViewChild(AceDirective) directiveRef?: AceDirective;

  config: AceConfigInterface = {
    mode: 'json',
    theme: 'eclipse',
    readOnly : false,
    fontSize: 16
  };
  inputSize = 'small';

  private content: string;
  /** unique key to store the config in the local storage */
  @Input() key ? = 'default';
  @Output() valueChange = new EventEmitter<string>();
  @Input()
  get value(): string {
    return this.content;
  }
  set value(value: string) {
    if (value !== this.content) {
      this.content = value;
      this.valueChange.emit(this.content);
    }
  }

  constructor() { }

  ngOnInit() {
    this.config.mode = localStorage.getItem('js-ui-ace-mode-' + this.key) || this.config.mode;
    this.inputSize = localStorage.getItem('js-ui-ace-size-' + this.key) || this.inputSize;
  }
  ngAfterContentInit(): void {
  }
  ngAfterViewChecked(): void {
    if (this.componentRef.directiveRef.getValue() !== this.content) {
      this.componentRef.directiveRef.setValue(this.content);
    }
  }
  onConfigChange(changes: any): void {
    localStorage.setItem('js-ui-ace-mode-' + this.key, this.config.mode);
    localStorage.setItem('js-ui-ace-size-' + this.key, this.inputSize);
  }

  doFormat() {
    let v = this.componentRef.directiveRef.getValue();
    if (this.config.mode === 'json') {
      v = JSON.parse(v);
      v = JSON.stringify(v, null, 2);
    } else if (this.config.mode === 'xml') {
      v = pd.pd.xml(v);
    }
    this.componentRef.directiveRef.setValue(v);
  }
}
