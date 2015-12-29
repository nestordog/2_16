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
package ch.algotrader.entity;

import java.util.Objects;

/**
 * A GenericEntity used for testing
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GenericItem implements BaseEntityI {

    private static final long serialVersionUID = 4933234000749259461L;

    @Override
    public boolean isInitialized() {
        return false;
    }

    private long id;

    private String name;

    private boolean active;

    private String broker;

    protected GenericItem() {

    }

    public GenericItem(final String name) {

        setName(name);
    }

    public GenericItem(final long id, final String name) {

        setId(id);
        setName(name);
    }

    @Override
    public long getId() {

        return this.id;
    }

    protected void setId(final long id) {

        this.id = id;
    }

    public String getName() {

        return this.name;
    }

    protected void setName(final String name) {

        this.name = name;
    }

    public boolean isActive() {

        return this.active;
    }

    public void setActive(final boolean active) {

        this.active = active;
    }

    public String getBroker() {

        return this.broker;
    }

    public void setBroker(final String broker) {

        this.broker = broker;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }
        if (obj instanceof GenericItem) {

            GenericItem that = (GenericItem) obj;

            return Objects.equals(this.getName(), that.getName());
        } else {

            return false;
        }

    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(getName());

        return hash;
    }

}