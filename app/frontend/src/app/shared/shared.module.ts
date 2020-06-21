import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingComponent } from './loading/loading.component';
import { LoadingButtonComponent } from './loading-button/loading-button.component';
import { ErrorMessagesComponent } from './error-messages/error-messages.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from '../material-module';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';



@NgModule({
  declarations: [LoadingComponent, LoadingButtonComponent, ErrorMessagesComponent],
  imports: [
    CommonModule,
    MatExpansionModule,
    MaterialModule
  ],
  exports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    LoadingComponent,
    LoadingButtonComponent,
    ErrorMessagesComponent,
    MaterialModule
  ]
})
export class SharedModule { }
