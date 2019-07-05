import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  public loading$ = new BehaviorSubject<boolean>(false);

  constructor() {}

  isLoading() {
    this.loading$.next(true);
  }
  finishedLoading() {
    this.loading$.next(false);
  }
}
