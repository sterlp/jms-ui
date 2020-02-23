export class JmsResource {
    name: string;
    type: JmsResourceType;
    vendorType: string;
}

export enum JmsResourceType {
    QUEUE,
    REMOTE_QUEUE,
    TOPIC
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
export interface SendJmsMessageCommand extends JmsMessage<JmsHeaderRequestValues> {}

export interface JmsResultMessage extends JmsMessage<JmsHeaderResultValues> {
    /** time it took to get the jms message */
    _time?: number;
}
