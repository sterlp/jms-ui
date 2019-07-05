import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { UsersComponent } from './pages/users/users.component';
import { ConnectorsComponent } from './pages/connectors/connectors.component';
import { SessionPageComponent } from './pages/session/session-page/session-page.component';
import { JmsMessagePageComponent } from './pages/session/jms-message-page/jms-message-page.component';

// https://blog.angularindepth.com/automatically-upgrade-lazy-loaded-angular-modules-for-ivy-e760872e6084
const routes: Routes = [
  { path: '', redirectTo: '/connectors', pathMatch: 'full' },
  { path: 'home', component: DashboardComponent },
  { path: 'connectors', component: ConnectorsComponent },
  { path: 'sessions/:id', component: SessionPageComponent },
  { path: 'sessions/:id/:target', component: JmsMessagePageComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
