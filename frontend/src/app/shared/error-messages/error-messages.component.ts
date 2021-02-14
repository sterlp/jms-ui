import { Component, OnInit, Input, Output } from '@angular/core';
import { EventEmitter } from 'events';

@Component({
  selector: 'app-error-messages',
  templateUrl: './error-messages.component.html',
  styleUrls: ['./error-messages.component.scss']
})
export class ErrorMessagesComponent implements OnInit {

    @Input() type = 'alert-warning';
    @Input() error: any;
    @Output() closed = new EventEmitter();

    constructor() { }

    ngOnInit(): void {}

    doClose() {
        this.closed.emit(this.error);
        this.error = null;
    }
}
