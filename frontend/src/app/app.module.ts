import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AceModule } from 'ngx-ace-wrapper';
import { ACE_CONFIG } from 'ngx-ace-wrapper';
import { AceConfigInterface } from 'ngx-ace-wrapper';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './dashboard/header/header.component';
import { SidebarComponent } from './dashboard/sidebar/sidebar.component';
import { ToggleDirective } from './dashboard/toggle.directive';
import { HomeComponent } from './page/home/home.component';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule} from './material-module';
import { ConnectorsComponent } from './page/connectors/connectors.component';
import { ConnectorViewComponent } from './components/connectors/connector-view/connector-view.component';
import { ErrorDialogComponent } from './common/error-dialog/error-dialog.component';
import { LoadingComponent } from './common/loading/loading.component';
import { SessionPageComponent } from './page/session/session-page/session-page.component';
import { ConfigFieldComponent } from './components/connectors/config-field/config-field.component';
import { HttpClientModule } from '@angular/common/http';
import { NgSpringBootApiModule } from 'projects/ng-spring-boot-api/src/public-api';
import { ConnectorPageComponent } from './page/connector.page/connector.page.component';
import { JmsMessagePageComponent } from './page/session/jms-message-page/jms-message-page.component';
import { JmsMessageComponent } from './components/jms/jms-message/jms-message.component';
import { JmsHeadersComponent } from './components/jms/jms-headers/jms-headers.component';
import { AceEditorComponent } from './common/ace-editor/ace-editor.component';
import { DatePipe } from '@angular/common';

const DEFAULT_ACE_CONFIG: AceConfigInterface = {
  tabSize: 2,
  fontSize: 16,
  showPrintMargin: false
};

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    SidebarComponent,
    ToggleDirective,
    ConfigFieldComponent,
    ErrorDialogComponent,
    LoadingComponent,
    HomeComponent,
    SessionPageComponent,
    ConnectorViewComponent,
    ConnectorsComponent,
    ConnectorPageComponent,
    JmsMessagePageComponent,
    JmsMessageComponent,
    JmsHeadersComponent,
    AceEditorComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    MaterialModule,
    HttpClientModule,
    NgSpringBootApiModule,
    AceModule
  ],
  providers: [DatePipe,
    { provide: ACE_CONFIG, useValue: DEFAULT_ACE_CONFIG }
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    ErrorDialogComponent
  ]
})
export class AppModule { }
