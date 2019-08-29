import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './page/home/home.component';
import { ConnectorsComponent } from './page/connectors/connectors.component';


const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: 'jms-connectors', component: ConnectorsComponent},
  { path: '', redirectTo: '/jms-connectors', pathMatch: 'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
