import { BehaviorSubject, Subscription } from 'rxjs';
import { OnDestroy } from '@angular/core';

export class ArrayUtils {
    static first<T>(values: T[]): T {
        // tslint:disable-next-line: curly
        if (values == null || values.length === 0) return null;
        return values[0];
    }
}

export class Subscriptions implements OnDestroy {
    private values: Subscription[] = [];

    ngOnDestroy(): void {
        this.unsubscribeAll();
    }

    manage(subscription: Subscription) {
        this.values.push(subscription);
    }

    unsubscribeAll() {
        this.values.forEach(e => {
            e.unsubscribe();
        });
        this.values.length = 0;
    }
}
