import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SessionPage } from './page/session-page/session.page';
import { JmsMessagePage } from './page/jms-message-page/jms-message.page';


const routes: Routes = [
    { path: ':id', component: SessionPage },
    { path: ':id/:target', component: JmsMessagePage},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SessionRoutingModule { }
