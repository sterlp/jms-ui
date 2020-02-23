import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ConnectorsPage } from './connectors/page/connectors/connectors.page';
import { ConnectorPage } from './connectors/page/connector/connector.page';


const routes: Routes = [
  {path: 'jms-connectors', component: ConnectorsPage},
  {path: 'jms-connector/:id', component: ConnectorPage},
  {path: 'jms-connector', component: ConnectorPage},
  {path: '', redirectTo: '/jms-connectors', pathMatch: 'full'},
  { path: 'sessions', loadChildren: () => import('./session/session.module').then(m => m.SessionModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
