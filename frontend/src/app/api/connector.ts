export enum ConfigType {
    STRING = 'STRING',
    NUMBER = 'NUMBER',
    BOOLEAN = 'BOOLEAN',
    PASSWORD = 'PASSWORD'
}

export interface SupportedConnector {
    id: string;
    name: string;
    configMeta: ConfigMetaData[];
}

export interface ConfigMetaData {
    property: string;
    label?: string;
    description?: string;
    defaultValue?: string;
    type: ConfigType;
    mandatory: boolean;
}

export interface ConnectorView {
    id?: number;
    type?: string;
    version?: number;
    name: string;
    clientName?: string;
}

export interface ConnectorData extends ConnectorView {
    timeout?: number;
    configValues?: Map<string, string>;
}
