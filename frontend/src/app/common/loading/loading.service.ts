import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  private loadingSub = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSub.asObservable();

  constructor() { }

  isLoading() {
    this.loadingSub.next(true);
  }
  finishedLoading() {
    this.loadingSub.next(false);
  }
}
