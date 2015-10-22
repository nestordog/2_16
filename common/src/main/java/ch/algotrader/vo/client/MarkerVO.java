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
package ch.algotrader.vo.client;

import java.io.Serializable;

/**
 * Contains a Marker Data Point
 */
public abstract class MarkerVO implements Serializable {

    private static final long serialVersionUID = 8402326054825580364L;

    /**
     * The name of the Marker. Has to match {@link MarkerDefinitionVO#getName}.
     */
    private String name;

    /**
     * Default Constructor
     */
    public MarkerVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param nameIn String
     */
    public MarkerVO(final String nameIn) {

        this.name = nameIn;
    }

    /**
     * Copies constructor from other MarkerVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public MarkerVO(final MarkerVO otherBean) {

        this.name = otherBean.getName();
    }

    /**
     * The name of the Marker. Has to match {@link MarkerDefinitionVO#getName}.
     * @return name String
     */
    public String getName() {

        return this.name;
    }

    /**
     * The name of the Marker. Has to match {@link MarkerDefinitionVO#getName}.
     * @param value String
     */
    public void setName(final String value) {

        this.name = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("MarkerVO [name=");
        builder.append(this.name);
        builder.append("]");

        return builder.toString();
    }

}
