import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { MatPaginator, MatSort, MatTableDataSource, MatDialog } from '@angular/material';
import { SupportedConnector, ConnectorData } from 'src/app/api/connector';
import { ArrayUtils } from 'src/app/common/utils';
import { ConnectorService, ConnertorDataSource } from 'src/app/components/connectors/connector.service';
import { catchError, map, tap, finalize } from 'rxjs/operators';
import { JmsSessionService } from 'src/app/components/jms-sessions/jms-session.service';
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

  supportedConnectors: SupportedConnector[] = [];
  selectedConnector: SupportedConnector;
  newConnector: SupportedConnector;
  newConnectorData: ConnectorData;
  dataSource: ConnertorDataSource;

  constructor(private $connector: ConnectorService, private $loading: LoadingService,
              private $session: JmsSessionService, private $router: Router, private dialog: MatDialog) { }

  ngOnInit() {
    this.dataSource = new ConnertorDataSource(this.$connector, this.$loading);
    this.$connector.getSupported().subscribe(result => {
      this.supportedConnectors = result;
      this.selectedConnector = ArrayUtils.first(this.supportedConnectors);
    });
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
  doEdit(data: ConnectorData) {
    this.$connector.getConnectorWithConfig(data.id).subscribe(c => {
      this.newConnector = this.supportedConnectors.find(e => e.id === data.type);
      this.newConnectorData = c;
    });
  }
  doCopy(data: ConnectorData) {
    this.$connector.getConnectorWithConfig(data.id).subscribe(c => {
      this.newConnector = this.supportedConnectors.find(e => e.id === data.type);
      c.id = null;
      c.name = 'Copy of ' + c.name;
      this.newConnectorData = c;
    });
  }
  doConnect(conData: ConnectorData) {
    this.$session.openSession(conData).subscribe(
      openSessions => {
        // only if we session was created
        if (openSessions.find( s => s.id === conData.id)) {
          this.$router.navigate(['/sessions', conData.id]);
        }
      },
    );
  }
  addConnector(type: SupportedConnector) {
    this.newConnector = type;
    this.newConnectorData = new ConnectorData();
    this.newConnectorData.type = this.newConnector.id;
    this.newConnectorData.name = this.newConnector.name;
  }
  cancelAdd() {
    this.newConnector = null;
  }
  doSave() {
    this.$connector.save(this.newConnectorData).subscribe(result => {
      this.newConnectorData = result;
      this.newConnector = null;
      this.doLoad();
    });
  }

  private showError(operation = '', error: any) {
    console.error(`${operation} failed: ${error.message}`, error); // log to console instead
    this.dialog.open(ErrorDialogComponent, {
      width: '250px',
      data: {error, operation}
    });
  }

  
}