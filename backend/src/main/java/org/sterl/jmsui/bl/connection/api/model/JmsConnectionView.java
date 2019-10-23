package org.sterl.jmsui.bl.connection.api.model;

public interface JmsConnectionView {

    long getId();
    String getName();
    String getType();
    String getClientName();
    long getVersion();
}
