import { BehaviorSubject } from 'rxjs';
import { Closable } from '@sterlp/ng-spring-boot-api';

export class LoadingHelper implements Closable {

    // tslint:disable-next-line: variable-name
    private _loading = new BehaviorSubject<boolean>(false);
    public readonly loading$ = this._loading.asObservable();

    // tslint:disable-next-line: variable-name
    private _requestCount = 0;

    constructor() {}

    get isLoading() { return this._requestCount > 0; }

    loading() {
        ++this._requestCount;
        this._loading.next(true);
    }

    done() {
        --this._requestCount;
        if (this._requestCount <= 0) {
            this._requestCount = 0;
            this._loading.next(false);
        }
    }

    close(): void {
        this._loading.complete();
    }
}
