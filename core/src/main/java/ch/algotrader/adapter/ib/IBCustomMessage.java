/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.ib;

import java.io.Serializable;

import org.apache.commons.lang.Validate;

/**
 * IB specific message.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IBCustomMessage implements Serializable {

    enum Type { GET_GROUPS }

    private static final long serialVersionUID = -3600626814458791107L;

    private final String id;
    private final Type type;
    private final String content;

    public IBCustomMessage(final String id, final Type type, final String content) {

        Validate.notEmpty(id, "Message ID is empty");
        Validate.notNull(type, "Message type is null");
        Validate.notEmpty(id, "Message content is empty");

        this.id = id;
        this.type = type;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("id='").append(id).append('\'');
        sb.append("type=").append(type);
        sb.append(", content='").append(content).append('\'');
        sb.append(']');
        return sb.toString();
    }

}
