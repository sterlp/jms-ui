package org.sterl.jmsui.bl.connectors.api.model;

import java.util.Comparator;

public class JmsResourceComperator implements Comparator<JmsResource> {
    @Override
    public int compare(JmsResource o1, JmsResource o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
