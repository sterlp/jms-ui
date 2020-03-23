import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SidebarComponent } from './dashboard/sidebar/sidebar.component';
import { ToggleDirective } from './dashboard/toggle.directive';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule} from './material-module';
import { ConnectorsPage } from './connectors/page/connectors/connectors.page';
import { ConnectorViewComponent } from './connectors/component/connector-view/connector-view.component';
import { ErrorDialogComponent } from './common/error-dialog/error-dialog.component';
import { ConfigFieldComponent } from './connectors/component/config-field/config-field.component';
import { HttpClientModule } from '@angular/common/http';
import { ConnectorPage } from './connectors/page/connector/connector.page';
import { DatePipe, LocationStrategy, HashLocationStrategy } from '@angular/common';
import { LoadingBarModule } from '@ngx-loading-bar/core';
import { LoadingBarHttpClientModule } from '@ngx-loading-bar/http-client';
import { SharedModule } from './shared/shared.module';

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
    FormsModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    MaterialModule,
    HttpClientModule,
    SharedModule,
    LoadingBarModule,
    LoadingBarHttpClientModule
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
