import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';


@Component({
  selector: 'app-loading-button',
  templateUrl: './loading-button.component.html',
  styleUrls: ['./loading-button.component.scss']
})
export class LoadingButtonComponent implements OnInit {

    @Input() loadingText ?= 'Loading ...';
    @Input() loading$: Observable<boolean>;
    @Output() doLoad: EventEmitter<any> = new EventEmitter();

    constructor() { }

    ngOnInit(): void {
    }

    doClick($event: any) {
        this.doLoad.emit($event);
    }
}
