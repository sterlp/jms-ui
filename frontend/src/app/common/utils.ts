import { BehaviorSubject, Subscription } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { validateBasis } from '@angular/flex-layout';

export class ArrayUtils {
    static first<T>(values: T[]): T {
        // tslint:disable-next-line: curly
        if (values == null || values.length === 0) return null;
        return values[0];
    }

    static forEach(value: {} | Map<string, any>): [string, any][] {
        let res: [string, any][] = [];
        if (value && 'forEach' in value) {
            const v = value as Map<string, any>;
            v.forEach( (x, y) => res.push( [y, x]) );
        } else if (value) {
            res = Object.entries(value);
        }
        return res;
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
