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
