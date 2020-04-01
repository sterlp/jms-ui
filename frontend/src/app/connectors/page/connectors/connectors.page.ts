import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { ConnectorService, ConnertorViewDataSource } from 'src/app/connectors/service/connector.service';
import { JmsSessionService } from 'src/app/session/service/session/jms-session.service';
import { Router } from '@angular/router';
import { ConnectorData, ConnectorView } from 'src/app/api/connector';
import { ErrorDialogService } from 'src/app/common/error-dialog/error-dialog.service';
import {animate, state, style, transition, trigger} from '@angular/animations';

@Component({
  templateUrl: './connectors.page.html',
  styleUrls: ['./connectors.page.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
// tslint:disable-next-line: component-class-suffix
export class ConnectorsPage implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;

  dataSource: ConnertorViewDataSource;
  connectingId: number;

  columnsToDisplay  = ['expand', 'name', 'timeout', 'clientName', 'action'];
  expandedElement: ConnectorView | null;

  constructor(private $connector: ConnectorService,
              private $session: JmsSessionService, private $router: Router, private errorDialog: ErrorDialogService) { }

  ngOnInit() {
    this.dataSource = new ConnertorViewDataSource(this.$connector);
    this.dataSource.loadConnectorData();
  }
  ngAfterViewInit(): void {
    this.dataSource.page$.subscribe(p => {
      this.paginator.length = p.totalElements;
    });
    this.paginator.page.subscribe(() => this.doLoad());
  }
  doLoad() {
    this.dataSource.loadConnectorData(this.paginator.pageIndex, this.paginator.pageSize);
  }

  doConnect(conData: ConnectorData) {
    this.connectingId = conData.id;
    this.$session.openSession(conData.id)
        .subscribe(
            openSessions => {
                // only if we session was created
                if (openSessions.find(s => s.id === conData.id)) {
                this.$router.navigate(['/sessions', conData.id]);
                }
            },
            e => {
                this.connectingId = null;
                this.errorDialog.openError(`Failed to open connection to ${conData.name}`, e.error || e)
            },
            () => this.connectingId = null
    );
  }
}
