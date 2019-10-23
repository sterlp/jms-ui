import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ConnectorData, SupportedConnector } from 'src/app/api/connector';
import { ConnectorService } from '../connector.service';
import { ArrayUtils } from 'src/app/common/utils';

/**
 * This component can display a ConnectorData
 */
@Component({
  selector: 'app-connector-view',
  templateUrl: './connector-view.component.html',
  styleUrls: ['./connector-view.component.scss']
})
/* tslint:disable:curly*/
export class ConnectorViewComponent implements OnInit {

  @Input() connectorData: ConnectorData;
  supported: SupportedConnector[];
  connectorMetaData: SupportedConnector;

  constructor(private connectorService: ConnectorService) { }

  ngOnInit() {
    if (this.connectorData == null) {
      this.connectorData = {name: 'new connector', configValues: new Map()};
    }
    this.connectorService.getSupported().subscribe(result => {
      this.supported = result;
      if (this.connectorData.type) {
        const selected = this.supported.filter(s => s.id === this.connectorData.type);
        if (selected.length > 0) this.connectorMetaData = selected[0];
      } else {
        this.doTypeChanged(ArrayUtils.first(this.supported));
      }
    });
  }
  doTypeChanged(val?: SupportedConnector) {
    if (val) this.connectorMetaData = val;
    if (this.connectorData && this.connectorMetaData) {
      this.connectorData.type = this.connectorMetaData.id;
    }
  }
}
