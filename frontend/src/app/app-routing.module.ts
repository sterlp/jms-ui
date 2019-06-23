import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { UsersComponent } from './pages/users/users.component';
import { ConnectorsComponent } from './pages/connectors/connectors.component';

// https://blog.angularindepth.com/automatically-upgrade-lazy-loaded-angular-modules-for-ivy-e760872e6084
const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: DashboardComponent },
  { path: 'users', component: UsersComponent },
  { path: 'connectors', component: ConnectorsComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
