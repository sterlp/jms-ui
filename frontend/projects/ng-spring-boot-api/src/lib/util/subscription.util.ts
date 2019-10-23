import { ReturnStatement } from '@angular/compiler';

export interface Closable {
    close(): void;
}

export class SubscriptionsHolder implements Closable {
    private toClose: Closable[] = [];
    get size(): number {
        return this.toClose.length;
    }
    add(val: Closable): SubscriptionsHolder {
        this.toClose.push(val);
        return this;
    }
    addAny(val: any): SubscriptionsHolder {
        if (val) {
            if (this.isFunction(val.unsubscribe)) {
                this.toClose.push({close() {val.unsubscribe(); }});
            } else if (this.isFunction(val.close)) {
                this.toClose.push(val);
            } else {
                throw new Error( (typeof val) + ' doesnt provide any known close method. ');
            }
        }
        return this;
    }
    close(): void {
        this.toClose.forEach(e => {
            try {
                e.close();
            } catch (error) {
                console.warn('Failed to close', e, 'with error', error);
            }
        });
        this.toClose = [];
    }

    isFunction(v: any): boolean {
        return v && typeof v === 'function';
    }
}
