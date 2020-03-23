import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingComponent } from './loading/loading.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LoadingButtonComponent } from './loading-button/loading-button.component';
import { MatButtonModule } from '@angular/material/button';
import { ErrorMessagesComponent } from './error-messages/error-messages.component';



@NgModule({
  declarations: [LoadingComponent, LoadingButtonComponent, ErrorMessagesComponent],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatButtonModule
  ],
  exports: [
    LoadingComponent,
    LoadingButtonComponent,
    ErrorMessagesComponent
  ]
})
export class SharedModule { }
