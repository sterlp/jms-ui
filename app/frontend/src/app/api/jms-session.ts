export interface JmsResource {
    name: string;
    type: JmsResourceType | string;
    vendorType?: string;
    // dynamically loaded in some areas ...
    _depth?: number;
}

/**
 * Checks if the given JmsResources are equal or not,
 */
// tslint:disable: curly
export function isSameJmsResource(v1: JmsResource, v2: JmsResource): boolean {
    if (v1 == null || v2 == null) return false;
    if (v1 === v2) return true;
    if (v1.name === v2.name && v1.type === v2.type) {
        if (v1.vendorType != null && v2.vendorType != null) {
            return v1.vendorType === v2.vendorType;
        } else {
            return true;
        }
    } else {
        return false;
    }
}

export class JmsResourceModel {
    static readonly COLUMNS = [
        { id: 'name',       header: 'Name',         cell: (e: JmsResource) => e.name        },
        { id: 'depth',      header: 'Depth',        cell: (e: JmsResource) => e._depth      },
        { id: 'type',       header: 'Type',         cell: (e: JmsResource) => e.type        }
    ];
}

export enum JmsResourceType {
    QUEUE = 'QUEUE',
    REMOTE_QUEUE = 'REMOTE_QUEUE',
    TOPIC = 'TOPIC'
}

export interface JmsHeader {
    JMSType: string;

    JMSDeliveryMode: number;
    JMSPriority: number;

    JMSTimestamp: number;
    JMSExpiration: number;

    JMSMessageID: string;
    JMSCorrelationID: string;

    properties?: Map<string, any>;
}
// tslint:disable-next-line: no-empty-interface
export interface JmsHeaderRequestValues extends JmsHeader {}

export interface JmsHeaderResultValues extends JmsHeader {
    JMSDeliveryTime: number;
    JMSRedelivered: boolean;
    JMSDestination: string;
    JMSReplyTo: string;
}
export interface JmsMessage<T extends JmsHeader> {
    body: string;
    header?: T;
}
export interface SendJmsMessageCommand extends JmsMessage<JmsHeaderRequestValues> {
    destination?: string;
    destinationType?: JmsResourceType;
}

export interface JmsResultMessage extends JmsMessage<JmsHeaderResultValues> {
    /** time it took to get the jms message */
    _time?: number;
}
