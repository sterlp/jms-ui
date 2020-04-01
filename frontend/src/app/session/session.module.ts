import { NgModule } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { SessionRoutingModule } from './session-routing.module';
import { SessionPage } from './page/session-page/session.page';
import { JmsMessagePage } from './page/jms-message-page/jms-message.page';
import { AceEditorComponent } from './component/ace-editor/ace-editor.component';
import { AceConfigInterface, ACE_CONFIG, AceModule } from 'ngx-ace-wrapper';
import { JmsMessageComponent } from './component/jms-message/jms-message.component';
import { JmsHeadersComponent } from './component/jms-headers/jms-headers.component';
import { SharedModule } from '../shared/shared.module';
import { BookmarksComponent } from './component/bookmarks/bookmarks.component';
import { JmsResourceListComponent } from './component/jms-resource-list/jms-resource-list.component';
import { JmsResourceDetailsComponent } from './component/jms-resource-details/jms-resource-details.component';

const DEFAULT_ACE_CONFIG: AceConfigInterface = {
    tabSize: 2,
    fontSize: 16,
    showPrintMargin: false
};

@NgModule({
  declarations: [
    SessionPage,
    JmsMessagePage,
    JmsMessageComponent,
    JmsHeadersComponent,
    AceEditorComponent,
    BookmarksComponent,
    JmsResourceListComponent,
    JmsResourceDetailsComponent
  ],
  imports: [
    SharedModule,
    SessionRoutingModule,
    AceModule,
  ],
  providers: [
    { provide: ACE_CONFIG, useValue: DEFAULT_ACE_CONFIG }, DecimalPipe
  ],
})
export class SessionModule { }
