import { LayoutModule } from '@angular/cdk/layout';
import { OverlayModule } from '@angular/cdk/overlay';
import { MaterialModule} from './material-module';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { TopnavComponent } from './components/topnav/topnav.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { UsersComponent } from './pages/users/users.component';
import { ConnectorsComponent } from './pages/connectors/connectors.component';
import { ConfigFieldComponent } from './components/connectors/config-field/config-field.component';
import { JsonPipe } from './common/pipe/json.pipe';
import { ConnectorViewComponent } from './components/connectors/connector-view/connector-view.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { SessionPageComponent } from './pages/session/session-page/session-page.component';
import { LoadingComponent } from './common/loading/loading.component';
import { ErrorDialogComponent } from './common/error-dialog/error-dialog.component';
import { JmsMessagePageComponent } from './pages/session/jms-message-page/jms-message-page.component';

@NgModule({
  declarations: [
    AppComponent,
    TopnavComponent,
    SidebarComponent,
    DashboardComponent,
    UsersComponent,
    ConnectorsComponent,
    ConfigFieldComponent,
    JsonPipe,
    ConnectorViewComponent,
    SessionPageComponent,
    LoadingComponent,
    ErrorDialogComponent,
    JmsMessagePageComponent
  ],
  imports: [
    BrowserModule,
    FontAwesomeModule,
    FormsModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    LayoutModule,
    OverlayModule,
    HttpClientModule,
    MaterialModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [
    ErrorDialogComponent
  ]
})
export class AppModule {
  constructor() {
    library.add(fas);
  }
}
