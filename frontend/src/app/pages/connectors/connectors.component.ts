import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Observable } from 'rxjs';
import { MatPaginator, MatSort, MatTableDataSource } from '@angular/material';
import { SupportedConnector, ConnectorData } from 'src/app/api/connector';
import { ArrayUtils } from 'src/app/common/utils'
import { ConnectorService, ConnertorDataSource } from 'src/app/components/connectors/connector.service';
import { catchError, map, tap, finalize } from 'rxjs/operators';

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

  constructor(private $connector: ConnectorService) { }

  ngOnInit() {
    this.dataSource = new ConnertorDataSource(this.$connector);
    this.$connector.getSupported().subscribe(result => {
      this.supportedConnectors = result;
      this.selectedConnector = ArrayUtils.first(this.supportedConnectors);
    });
    this.dataSource.loadConnectorData();
  }
  ngAfterViewInit(): void {
    this.dataSource.$page.subscribe(p => {
      this.paginator.length = p.totalElements;
    });
    this.paginator.page.subscribe(() => this.doLoad());
  }
  doLoad() {
    this.dataSource.loadConnectorData(this.paginator.pageIndex, this.paginator.pageSize);
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
}