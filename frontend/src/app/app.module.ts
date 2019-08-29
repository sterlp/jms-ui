import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

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
  ],
  imports: [
    BrowserModule,
    FormsModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    MaterialModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [
    ErrorDialogComponent
  ]
})
export class AppModule { }
