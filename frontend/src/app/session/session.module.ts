import { NgModule } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';

import { SessionRoutingModule } from './session-routing.module';
import { SessionPage } from './page/session-page/session.page';
import { JmsMessagePage } from './page/jms-message-page/jms-message.page';
import { AceEditorComponent } from './component/ace-editor/ace-editor.component';
import { AceConfigInterface, ACE_CONFIG, AceModule } from 'ngx-ace-wrapper';
import { MaterialModule } from '../material-module';
import { JmsMessageComponent } from './component/jms-message/jms-message.component';
import { JmsHeadersComponent } from './component/jms-headers/jms-headers.component';
import { SharedModule } from '../shared/shared.module';
import { BookmarksComponent } from './component/bookmarks/bookmarks.component';
import { FormsModule } from '@angular/forms';
import { LoadingButtonComponent } from '../shared/loading-button/loading-button.component';
import { JmsResourceListComponent } from './component/jms-resource-list/jms-resource-list.component';

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
    JmsResourceListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SessionRoutingModule,
    AceModule,
    MaterialModule,
    SharedModule
  ],
  providers: [
    { provide: ACE_CONFIG, useValue: DEFAULT_ACE_CONFIG }, DecimalPipe
  ],
})
export class SessionModule { }
