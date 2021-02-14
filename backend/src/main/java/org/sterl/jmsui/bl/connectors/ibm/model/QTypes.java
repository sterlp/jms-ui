package org.sterl.jmsui.bl.connectors.ibm.model;

import com.ibm.mq.constants.MQConstants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
/**
 * https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_9.1.0/com.ibm.mq.ref.adm.doc/q087870_.htm
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum QTypes {
    /** Alias queue definition. */
    MQQT_ALIAS(MQConstants.MQQT_ALIAS),
    /** Local queue. */
    MQQT_LOCAL(MQConstants.MQQT_LOCAL),
    /** Local definition of a remote queue. */
    MQQT_REMOTE(MQConstants.MQQT_REMOTE),
    /** Model queue definition. */
    MQQT_MODEL(MQConstants.MQQT_MODEL)
    
    ;
    private final int type;
    
    public static QTypes from(int value) {
        if (value == MQQT_ALIAS.type) return MQQT_ALIAS;
        if (value == MQQT_REMOTE.type) return MQQT_REMOTE;
        if (value == MQQT_MODEL.type) return MQQT_MODEL;
        else return MQQT_LOCAL;
    }
}
