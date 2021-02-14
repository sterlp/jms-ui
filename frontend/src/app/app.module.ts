import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SidebarComponent } from './dashboard/sidebar/sidebar.component';
import { ToggleDirective } from './dashboard/toggle.directive';
import { ConnectorsPage } from './connectors/page/connectors/connectors.page';
import { ConnectorViewComponent } from './connectors/component/connector-view/connector-view.component';
import { ErrorDialogComponent } from './common/error-dialog/error-dialog.component';
import { ConfigFieldComponent } from './connectors/component/config-field/config-field.component';
import { ConnectorPage } from './connectors/page/connector/connector.page';
import { DatePipe, LocationStrategy, HashLocationStrategy, CommonModule } from '@angular/common';
import { LoadingBarModule } from '@ngx-loading-bar/core';
import { LoadingBarHttpClientModule } from '@ngx-loading-bar/http-client';
import { SharedModule } from './shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';

@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    ToggleDirective,
    ConfigFieldComponent,
    ErrorDialogComponent,
    ConnectorViewComponent,
    ConnectorsPage,
    ConnectorPage
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    SharedModule,
    AppRoutingModule,
    LoadingBarModule,
    LoadingBarHttpClientModule,
  ],
  providers: [DatePipe,
    {
      provide: LocationStrategy,
      useClass: HashLocationStrategy
    }
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    ErrorDialogComponent
  ]
})
export class AppModule { }
