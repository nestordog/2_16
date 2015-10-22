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
 * Contains Annotation Data
 */
public abstract class AnnotationVO implements Serializable {

    private static final long serialVersionUID = 6709365527250784855L;

    /**
     * Default Constructor
     */
    public AnnotationVO() {

        // documented empty block - avoid compiler warning
    }

    /**
     * Copies constructor from other AnnotationVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public AnnotationVO(final AnnotationVO otherBean) {

        // documented empty block - avoid compiler warning
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("AnnotationVO []");

        return builder.toString();
    }

}
