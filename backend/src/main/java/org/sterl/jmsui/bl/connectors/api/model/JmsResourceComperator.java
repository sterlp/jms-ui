package org.sterl.jmsui.bl.connectors.api.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JmsResourceComperator implements Comparator<JmsResource> {
    public static final JmsResourceComperator INSTANCE = new JmsResourceComperator();

    public static final void sort(List<JmsResource> toSort) {
        Collections.sort(toSort, INSTANCE);
    }

    @Override
    public int compare(JmsResource o1, JmsResource o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
