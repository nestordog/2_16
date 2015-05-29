/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity;

import java.util.Objects;

import ch.algotrader.enumeration.Broker;
import ch.algotrader.visitor.EntityVisitor;

/**
 * A GenericEntity used for testing
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericItem implements BaseEntityI {

    private static final long serialVersionUID = 4933234000749259461L;

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public <R, P> R accept(EntityVisitor<R, ? super P> visitor, P param) {
        throw new UnsupportedOperationException("accept not supported");
    }

    private long id;

    private String name;

    private boolean active;

    private Broker broker;

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

    public Broker getBroker() {

        return this.broker;
    }

    public void setBroker(final Broker broker) {

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