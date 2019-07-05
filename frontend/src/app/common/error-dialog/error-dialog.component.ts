import { Component, OnInit, Input, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

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

  getError() {
    return this.data.error.error.message || this.data.error.message || this.data.error.statusText;
  }

  getErrorDetails() {
    return this.data.error.error.trace || null;
  }
}
