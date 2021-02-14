import { JmsResource, JmsResourceType } from 'src/app/api/jms-session';

export interface Bookmark {
    id?: number;
    name?: string;
    type?: string | JmsResourceType;
}
