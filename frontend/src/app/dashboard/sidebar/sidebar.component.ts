import { Component, OnInit, Input, Inject, Renderer2, OnDestroy, HostBinding, ElementRef, HostListener } from '@angular/core';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { ConnectorView } from 'src/app/api/connector';
import { JmsResource } from 'src/app/api/jms-session';

/**
 * https://coreui.io/v1/docs/layout/options/
 */
@Component({
    selector: '[appSidebar]',
    host: {
        'class': 'c-sidebar c-sidebar-dark'
    },
    templateUrl: './sidebar.component.html'
})
// tslint:disable: curly variable-name
export class SidebarComponent implements OnInit {

    @HostBinding('class.c-sidebar-show') _alwaysShow = false;
    @HostBinding('class.c-sidebar-lg-show') _show = true;
    private _enableClickOutside = false;
    @Input()
    @HostBinding('class.c-sidebar-fixed') fixed = true;
    sessions: ConnectorView[] = [];
    private openResources: Map<number, JmsResource[]>;

  constructor(
    private eRef: ElementRef,
    private sessionsService: JmsSessionService) { }

    ngOnInit() {
        this.sessionsService.sessions$.subscribe(s => this.sessions = s);
        this.openResources = this.sessionsService.openResources;
    }

    getResources(connectorId: number): JmsResource[] {
        let result = this.openResources.get(connectorId);
        if (result == null) result = [];
        return result;
    }

    toggle(): void {
        const smalScreen = window && window.innerWidth <= 992;
        if (smalScreen) {
            if (this._alwaysShow) {
                this._alwaysShow = false;
                this._show = false;
            } else {
                this._show = true;
                this._alwaysShow = true;
                this._enableClickOutside = false;
                setTimeout(() => this._enableClickOutside = true, 150);
            }
        } else {
            if (this._show || this._alwaysShow) {
                this._alwaysShow = false;
                this._show = false;
            } else {
                this._show = true;
            }
        }
    }
    @HostListener('document:click', ['$event'])
    clickout(event: any) {
        if (this._alwaysShow && this._enableClickOutside) {
            if (this.eRef.nativeElement.contains(event.target)) {
                // clicked inside
            } else {
                // clicked outside
                this._alwaysShow = false;
            }
        }
    }
}
