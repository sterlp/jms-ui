import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './page/home/home.component';
import { ConnectorsComponent } from './page/connectors/connectors.component';
import { ConnectorPageComponent } from './page/connector.page/connector.page.component';


const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: 'jms-connectors', component: ConnectorsComponent},
  {path: 'jms-connector', component: ConnectorPageComponent},
  {path: 'jms-connector/:id', component: ConnectorPageComponent},
  { path: '', redirectTo: '/jms-connectors', pathMatch: 'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
