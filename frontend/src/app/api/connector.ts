export enum ConfigType {
    STRING = 'STRING',
    NUMBER = 'NUMBER',
    BOOLEAN = 'BOOLEAN',
    PASSWORD = 'PASSWORD'
}

export class SupportedConnector {
    id: string;
    name: string;
    configMeta: ConfigMetaData[];
}

export class ConfigMetaData {
    property: string;
    label?: string;
    description?: string;
    defaultValue?: string;
    type: ConfigType = ConfigType.STRING;
    mandatory = true;
}

export class ConnectorData {
    id?: string;
    type: string;
    version?: number;
    name: string;
    clientName: string;
    timeout: number;
    configValues?: Map<string, string> = new Map();
}
/**
 * Haetos generic result type of the jms-connections endpoint.
 */
export class ConnectorDataResource {
    jmsConnections: ConnectorData[];
}
