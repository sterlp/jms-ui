import { Component, OnInit, Input, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable } from 'rxjs';

export interface ErrorDialogData {
  error: any;
  operation: string;
}

@Component({
  selector: 'app-error-dialog',
  templateUrl: './error-dialog.component.html'
})
export class ErrorDialogComponent implements OnInit {

    constructor(
        public dialogRef: MatDialogRef<ErrorDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: ErrorDialogData) { }

    ngOnInit() {
    }

    getErrorMessage() {
        return this.error.message || this.error.statusText;
    }

    getErrorDetails() {
        if (this.error.trace !== '') {
            return this.error.trace;
        }
        return null;
    }

    get error(): any {
        return this.data.error.headers ? this.data.error :
            this.data.error.error && this.data.error.error.headers ? this.data.error.error : this.data.error;
    }
}
