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
package ch.algotrader.dao;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang.Validate;

/**
 * Named query parameter.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class NamedParam implements Serializable {

    private static final long serialVersionUID = -1987464357175551952L;

    private final String name;
    private final Object value;


    public NamedParam(String name, Object value) {
        Validate.notNull(name, "Name is null");
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[")
                .append(this.name).append(": ").append(this.value)
                .append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash  = 17;
        hash = hash * 37 + Objects.hashCode(getName());
        hash = hash * 37 + Objects.hashCode(getValue());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof NamedParam)) {
            return false;
        } else {
            NamedParam that = (NamedParam) obj;
            return Objects.equals(this.getName(), that.getName()) &&
            Objects.equals(this.getValue(), that.getValue());
        }
    }

}
