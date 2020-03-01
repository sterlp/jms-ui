import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingComponent } from './loading/loading.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LoadingButtonComponent } from './loading-button/loading-button.component';
import { MatButtonModule } from '@angular/material/button';



@NgModule({
  declarations: [LoadingComponent, LoadingButtonComponent],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatButtonModule
  ],
  exports: [
    LoadingComponent,
    LoadingButtonComponent
  ]
})
export class SharedModule { }
