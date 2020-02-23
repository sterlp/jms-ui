import { Component, OnInit, Input, Inject, Renderer2, OnDestroy } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { ConnectorView } from 'src/app/api/connector';

/**
 * https://coreui.io/v1/docs/layout/options/
 */
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit, OnDestroy {

  private sidebarMinimized = false;
  sessions: ConnectorView[] = [];

  constructor(
    @Inject(DOCUMENT) private document: any,
    private renderer: Renderer2,
    private sessionsService: JmsSessionService) { }

  // make the side bar full hight
  // app-sidebar-nav-divider
  ngOnInit() {
      this.renderer.addClass(this.document.body, 'sidebar-fixed');
      this.sessionsService.sessions$.subscribe(s => this.sessions = s);
  }

  ngOnDestroy(): void {
    this.renderer.removeClass(this.document.body, 'sidebar-fixed');
  }

  // tslint:disable: curly
  doMinimize() {
    if (this.sidebarMinimized) this.renderer.removeClass(this.document.body, 'sidebar-minimized');
    else this.renderer.addClass(this.document.body, 'sidebar-minimized');
    this.sidebarMinimized = !this.sidebarMinimized;
  }
  showMinimize(): boolean {
    return this.document && this.document.body;
  }
}
