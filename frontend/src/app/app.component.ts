import { Component, OnInit, ChangeDetectorRef, AfterViewChecked } from '@angular/core';
import { LoadingService } from './common/loading/loading.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements AfterViewChecked {
  loading$: any;

  constructor(private $loading: LoadingService, private cdRef : ChangeDetectorRef) {}

  ngAfterViewChecked(): void {
    this.loading$ = this.$loading.loading$;
    // https://stackoverflow.com/questions/43513421/ngif-expression-has-changed-after-it-was-checked
    this.cdRef.detectChanges();
  }
}
