import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './page/home/home.component';
import { ConnectorsComponent } from './page/connectors/connectors.component';
import { ConnectorPageComponent } from './page/connector.page/connector.page.component';
import { SessionPageComponent } from './page/session/session-page/session-page.component';
import { JmsMessagePageComponent } from './page/session/jms-message-page/jms-message-page.component';


const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: 'jms-connectors', component: ConnectorsComponent},
  {path: 'jms-connector/:id', component: ConnectorPageComponent},
  {path: 'jms-connector', component: ConnectorPageComponent},
  {path: 'sessions/:id', component: SessionPageComponent},
  {path: 'sessions/:id/:target', component: JmsMessagePageComponent},
  {path: '', redirectTo: '/jms-connectors', pathMatch: 'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
