import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConnectorService } from 'src/app/connectors/service/connector.service';
import { ConnectorData } from 'src/app/api/connector';
import { SubscriptionsHolder } from '@sterlp/ng-spring-boot-api';

@Component({
  selector: 'app-connector.page',
  templateUrl: './connector.page.html',
  styleUrls: ['./connector.page.scss']
})
/* tslint:disable:curly component-class-suffix*/
export class ConnectorPage implements OnInit, OnDestroy {
  BACK_URL = ['/jms-connectors'];

  private subs = new SubscriptionsHolder();
  @Input() connectorData: ConnectorData;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private connectorService: ConnectorService) { }

  ngOnInit() {
    this.subs.add(this.route.params.subscribe(params => {
      const id = params.id * 1;
      if (id) {
        this.connectorService.getConnectorWithConfig(id).subscribe(d => this.connectorData = d);
      } else {
        this.connectorData = {name: 'New Connector', configValues: new Map()};
      }
    }));
  }

  ngOnDestroy(): void {
    this.subs.close();
  }

  doSave(close?: boolean) {
    this.connectorService.save(this.connectorData).subscribe(result => {
      this.connectorData = result;
      if (close) this.router.navigate(this.BACK_URL);
    });
  }
  doDelete() {
    this.connectorService.delete(this.connectorData.id).subscribe(result => {
      this.router.navigate(this.BACK_URL);
    });
  }
  doCopy() {
    this.connectorData.id = null;
    this.connectorData.name = 'Copy of ' + this.connectorData.name;
  }
}
