package org.sterl.jmsui.bl.connectors.ibm.common;

import com.ibm.mq.MQQueue;

/**
 * Addition to the common JmsUtil for IBM
 */
public class IbmResourceHelper {

    public static Exception close(MQQueue queue) {
        Exception result = null;
        if (queue != null) {
            try {
                queue.close();
            } catch (Exception e) {
                result = e;
            }
        }
        return result;
    }
}
