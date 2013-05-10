// license-header java merge-point
//
/**
 * @author Generated on 08/03/2012 15:51:50+0200 Do not modify by hand!
 *
 * TEMPLATE:     ValueObject.vsl in andromda-java-cartridge.
 * MODEL CLASS:  Data::AlgoTrader::com.algoTrader::vo::ib::TickPrice
 * STEREOTYPE:   AlgoTraderValueObject
 * STEREOTYPE:   ValueObject
 */
/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.adapter.ib;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * TODO: Model Documentation for class TickPrice
 */
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickPrice
    implements Serializable, Comparable<TickPrice>
{
    /** The serial version UID of this class. Needed for serialization. */
    private static final long serialVersionUID = -4096826680075105503L;

    // Class attributes
    /** TODO: Model Documentation for attribute tickerId */
    protected int tickerId;
    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    protected boolean setTickerId = false;
    /** TODO: Model Documentation for attribute field */
    protected int field;
    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    protected boolean setField = false;
    /** TODO: Model Documentation for attribute price */
    protected double price;
    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    protected boolean setPrice = false;
    /** TODO: Model Documentation for attribute canAutoExecute */
    protected int canAutoExecute;
    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    protected boolean setCanAutoExecute = false;

    /** Default Constructor with no properties */
    public TickPrice()
    {
        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param tickerIdIn int
     * @param fieldIn int
     * @param priceIn double
     * @param canAutoExecuteIn int
     */
    public TickPrice(final int tickerIdIn, final int fieldIn, final double priceIn, final int canAutoExecuteIn)
    {
        this.tickerId = tickerIdIn;
        this.setTickerId = true;
        this.field = fieldIn;
        this.setField = true;
        this.price = priceIn;
        this.setPrice = true;
        this.canAutoExecute = canAutoExecuteIn;
        this.setCanAutoExecute = true;
    }

    /**
     * Copies constructor from other TickPrice
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TickPrice(final TickPrice otherBean)
    {
        this.tickerId = otherBean.getTickerId();
        this.setTickerId = true;
        this.field = otherBean.getField();
        this.setField = true;
        this.price = otherBean.getPrice();
        this.setPrice = true;
        this.canAutoExecute = otherBean.getCanAutoExecute();
        this.setCanAutoExecute = true;
    }

    /**
     * Copies all properties from the argument value object into this value object.
     * @param otherBean Cannot be <code>null</code>
     */
    public void copy(final TickPrice otherBean)
    {
        if (null != otherBean)
        {
            this.setTickerId(otherBean.getTickerId());
            this.setTickerId = true;
            this.setField(otherBean.getField());
            this.setField = true;
            this.setPrice(otherBean.getPrice());
            this.setPrice = true;
            this.setCanAutoExecute(otherBean.getCanAutoExecute());
            this.setCanAutoExecute = true;
        }
    }

    /**
     * TODO: Model Documentation for attribute tickerId
     * Get the tickerId Attribute
     * @return tickerId int
     */
    public int getTickerId()
    {
        return this.tickerId;
    }

    /**
     *
     * @param value int
     */
    public void setTickerId(final int value)
    {
        this.tickerId = value;
        this.setTickerId = true;
    }

    /**
     * Return true if the primitive attribute tickerId is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetTickerId()
    {
        return this.setTickerId;
    }

    /**
     * TODO: Model Documentation for attribute field
     * Get the field Attribute
     * @return field int
     */
    public int getField()
    {
        return this.field;
    }

    /**
     *
     * @param value int
     */
    public void setField(final int value)
    {
        this.field = value;
        this.setField = true;
    }

    /**
     * Return true if the primitive attribute field is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetField()
    {
        return this.setField;
    }

    /**
     * TODO: Model Documentation for attribute price
     * Get the price Attribute
     * @return price double
     */
    public double getPrice()
    {
        return this.price;
    }

    /**
     *
     * @param value double
     */
    public void setPrice(final double value)
    {
        this.price = value;
        this.setPrice = true;
    }

    /**
     * Return true if the primitive attribute price is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetPrice()
    {
        return this.setPrice;
    }

    /**
     * TODO: Model Documentation for attribute canAutoExecute
     * Get the canAutoExecute Attribute
     * @return canAutoExecute int
     */
    public int getCanAutoExecute()
    {
        return this.canAutoExecute;
    }

    /**
     *
     * @param value int
     */
    public void setCanAutoExecute(final int value)
    {
        this.canAutoExecute = value;
        this.setCanAutoExecute = true;
    }

    /**
     * Return true if the primitive attribute canAutoExecute is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetCanAutoExecute()
    {
        return this.setCanAutoExecute;
    }

    /**
     * @param object to compare this object against
     * @return boolean if equal
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object object)
    {
        if (object==null || object.getClass() != this.getClass())
        {
             return false;
        }
        // Check if the same object instance
        if (object==this)
        {
            return true;
        }
        TickPrice rhs = (TickPrice) object;
        return new EqualsBuilder()
            .append(this.getTickerId(), rhs.getTickerId())
            .append(this.getField(), rhs.getField())
            .append(this.getPrice(), rhs.getPrice())
            .append(this.getCanAutoExecute(), rhs.getCanAutoExecute())
            .isEquals();
    }

    /**
     * @param object to compare this object against
     * @return int if equal
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(final TickPrice object)
    {
        if (object==null)
        {
            return -1;
        }
        // Check if the same object instance
        if (object==this)
        {
            return 0;
        }
        return new CompareToBuilder()
            .append(this.getTickerId(), object.getTickerId())
            .append(this.getField(), object.getField())
            .append(this.getPrice(), object.getPrice())
            .append(this.getCanAutoExecute(), object.getCanAutoExecute())
            .toComparison();
    }

    /**
     * @return int hashCode value
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(1249046965, -82296885)
            .append(this.getTickerId())
            .append(this.getField())
            .append(this.getPrice())
            .append(this.getCanAutoExecute())
            .toHashCode();
    }

    /**
     * @return String representation of object
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("tickerId", this.getTickerId())
            .append("field", this.getField())
            .append("price", this.getPrice())
            .append("canAutoExecute", this.getCanAutoExecute())
            .toString();
    }

    /**
     * Compares the properties of this instance to the properties of the argument. This method will return
     * {@code false} as soon as it detects that the argument is {@code null} or not of the same type as
     * (or a sub-type of) this instance's type.
     *
     * <p/>For array, collection or map properties the comparison will be done one level deep, in other words:
     * the elements will be compared using the {@code equals()} operation.
     *
     * <p/>Note that two properties will be considered equal when both values are {@code null}.
     *
     * @param thatObject the object containing the properties to compare against this instance
     * @return this method will return {@code true} in case the argument has the same type as this class, or is a
     *      sub-type of this class and all properties as found on this class have equal values when queried on that
     *      argument instance; in all other cases this method will return {@code false}
     */
    public boolean equalProperties(final Object thatObject)
    {
        if (thatObject == null || !this.getClass().isAssignableFrom(thatObject.getClass()))
        {
            return false;
        }

        final TickPrice that = (TickPrice)thatObject;

        return
            equal(this.getTickerId(), that.getTickerId())
            && equal(this.getField(), that.getField())
            && equal(this.getPrice(), that.getPrice())
            && equal(this.getCanAutoExecute(), that.getCanAutoExecute())
        ;
    }

    /**
     * This is a convenient helper method which is able to detect whether or not two values are equal. Two values
     * are equal when they are both {@code null}, are arrays of the same length with equal elements or are
     * equal objects (this includes {@link java.util.Collection} and {@link java.util.Map} instances).
     *
     * <p/>Note that for array, collection or map instances the comparison runs one level deep.
     *
     * @param first the first object to compare, may be {@code null}
     * @param second the second object to compare, may be {@code null}
     * @return this method will return {@code true} in case both objects are equal as explained above;
     *      in all other cases this method will return {@code false}
     */
    protected static boolean equal(final Object first, final Object second)
    {
        final boolean equal;

        if (first == null)
        {
            equal = (second == null);
        }
        else if (first.getClass().isArray() && (second != null) && second.getClass().isArray())
        {
            equal = Arrays.equals((Object[])first, (Object[])second);
        }
        else // note that the following also covers java.util.Collection and java.util.Map
        {
            equal = first.equals(second);
        }

        return equal;
    }

    // TickPrice value-object java merge-point
}
