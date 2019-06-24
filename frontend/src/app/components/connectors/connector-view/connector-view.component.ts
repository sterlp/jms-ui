import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ConnectorData, SupportedConnector } from 'src/app/api/connector';

/**
 * This component can display a ConnectorData
 */
@Component({
  selector: 'app-connector-view',
  templateUrl: './connector-view.component.html',
  styleUrls: ['./connector-view.component.scss']
})
export class ConnectorViewComponent implements OnInit {

  @Input() connectorData: ConnectorData;
  @Input() connectorMetaData: SupportedConnector;

  constructor() { }

  ngOnInit() {
    if (this.connectorData == null) {
      this.connectorData = new ConnectorData();
    }
  }
}
