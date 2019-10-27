import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { MatPaginator, MatSort, MatTableDataSource, MatDialog } from '@angular/material';
import { SupportedConnector, ConnectorData } from 'src/app/api/connector';
import { ArrayUtils } from 'src/app/common/utils';
import { ConnectorService, ConnertorViewDataSource } from 'src/app/components/connectors/connector.service';
import { catchError, map, tap, finalize } from 'rxjs/operators';
import { JmsSessionService } from 'src/app/page/session/jms-session.service';
import { Router } from '@angular/router';
import { LoadingService } from 'src/app/common/loading/loading.service';
import { ErrorDialogComponent } from 'src/app/common/error-dialog/error-dialog.component';

@Component({
  selector: 'app-connectors',
  templateUrl: './connectors.component.html',
  styleUrls: ['./connectors.component.scss']
})
export class ConnectorsComponent implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;

  dataSource: ConnertorViewDataSource;

  constructor(private $connector: ConnectorService, private $loading: LoadingService,
              private $session: JmsSessionService, private $router: Router, private dialog: MatDialog) { }

  ngOnInit() {
    this.dataSource = new ConnertorViewDataSource(this.$connector, this.$loading);
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
    this.$session.openSession(conData.id).subscribe(
      openSessions => {
        // only if we session was created
        if (openSessions.find(s => s.id === conData.id)) {
          this.$router.navigate(['/sessions', conData.id]);
        }
      },
    );
  }
}
