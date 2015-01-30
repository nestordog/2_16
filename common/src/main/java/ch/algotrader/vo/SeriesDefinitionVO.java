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
package ch.algotrader.vo;

import java.io.Serializable;

import ch.algotrader.enumeration.Color;

/**
 * Defines a Chart Series
 */
public abstract class SeriesDefinitionVO implements Serializable {

    private static final long serialVersionUID = 3707067949585962245L;

    /**
     * The Series display Label.
     */
    private String label;

    /**
     * Should this Series be displayed by default.
     */
    private boolean selected;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setSelected = false;

    /**
     * The Color of this Series.
     */
    private Color color;

    /**
     * Should this Series Line be dashed? Only valid for Indicator Series.
     */
    private boolean dashed;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setDashed = false;

    /**
     * Default Constructor
     */
    public SeriesDefinitionVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param labelIn String
     * @param selectedIn boolean
     * @param colorIn Color
     * @param dashedIn boolean
     */
    public SeriesDefinitionVO(final String labelIn, final boolean selectedIn, final Color colorIn, final boolean dashedIn) {

        this.label = labelIn;
        this.selected = selectedIn;
        this.setSelected = true;
        this.color = colorIn;
        this.dashed = dashedIn;
        this.setDashed = true;
    }

    /**
     * Copies constructor from other SeriesDefinitionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SeriesDefinitionVO(final SeriesDefinitionVO otherBean) {

        this.label = otherBean.getLabel();
        this.selected = otherBean.isSelected();
        this.setSelected = true;
        this.color = otherBean.getColor();
        this.dashed = otherBean.isDashed();
        this.setDashed = true;
    }

    /**
     * The Series display Label.
     * @return label String
     */
    public String getLabel() {

        return this.label;
    }

    /**
     * The Series display Label.
     * @param value String
     */
    public void setLabel(final String value) {

        this.label = value;
    }

    /**
     * Should this Series be displayed by default.
     * @return selected boolean
     */
    public boolean isSelected() {

        return this.selected;
    }

    /**
     * Should this Series be displayed by default.
     * Duplicates isBoolean method, for use as Jaxb2 compatible object
     * @return selected boolean
     */
    @Deprecated
    public boolean getSelected() {

        return this.selected;
    }

    /**
     * Should this Series be displayed by default.
     * @param value boolean
     */
    public void setSelected(final boolean value) {

        this.selected = value;
        this.setSelected = true;
    }

    /**
     * Return true if the primitive attribute selected is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetSelected() {

        return this.setSelected;
    }

    /**
     * The Color of this Series.
     * @return color Color
     */
    public Color getColor() {

        return this.color;
    }

    /**
     * The Color of this Series.
     * @param value Color
     */
    public void setColor(final Color value) {

        this.color = value;
    }

    /**
     * Should this Series Line be dashed? Only valid for Indicator Series.
     * @return dashed boolean
     */
    public boolean isDashed() {

        return this.dashed;
    }

    /**
     * Should this Series Line be dashed? Only valid for Indicator Series.
     * Duplicates isBoolean method, for use as Jaxb2 compatible object
     * @return dashed boolean
     */
    @Deprecated
    public boolean getDashed() {

        return this.dashed;
    }

    /**
     * Should this Series Line be dashed? Only valid for Indicator Series.
     * @param value boolean
     */
    public void setDashed(final boolean value) {

        this.dashed = value;
        this.setDashed = true;
    }

    /**
     * Return true if the primitive attribute dashed is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetDashed() {

        return this.setDashed;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SeriesDefinitionVO [label=");
        builder.append(this.label);
        builder.append(", selected=");
        builder.append(this.selected);
        builder.append(", setSelected=");
        builder.append(this.setSelected);
        builder.append(", color=");
        builder.append(this.color);
        builder.append(", dashed=");
        builder.append(this.dashed);
        builder.append(", setDashed=");
        builder.append(this.setDashed);
        builder.append("]");

        return builder.toString();
    }

}
