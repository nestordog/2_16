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
import java.util.ArrayList;
import java.util.Collection;

import ch.algotrader.enumeration.DatasetType;

/**
 * Defines a Chart Dataset
 */
public class DatasetDefinitionVO implements Serializable {

    private static final long serialVersionUID = -6738935371318659791L;

    /**
     * The type of Dataset, either {@code TIME} or {@code OHLC}.
     */
    private DatasetType type;

    /**
     * Defines a Chart Series
     */
    private Collection<SeriesDefinitionVO> seriesDefinitions;

    /**
     * Default Constructor
     */
    public DatasetDefinitionVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param typeIn DatasetType The type of Dataset, either {@code TIME} or {@code OHLC}.
     */
    public DatasetDefinitionVO(final DatasetType typeIn) {

        this.type = typeIn;
    }

    /**
     * Constructor with all properties
     * @param typeIn DatasetType
     * @param seriesDefinitionsIn Collection<SeriesDefinitionVO>
     */
    public DatasetDefinitionVO(final DatasetType typeIn, final Collection<SeriesDefinitionVO> seriesDefinitionsIn) {

        this.type = typeIn;
        this.seriesDefinitions = seriesDefinitionsIn;
    }

    /**
     * Copies constructor from other DatasetDefinitionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public DatasetDefinitionVO(final DatasetDefinitionVO otherBean) {

        this.type = otherBean.getType();
        this.seriesDefinitions = otherBean.getSeriesDefinitions();
    }

    /**
     * The type of Dataset, either {@code TIME} or {@code OHLC}.
     * @return type DatasetType
     */
    public DatasetType getType() {

        return this.type;
    }

    /**
     * The type of Dataset, either {@code TIME} or {@code OHLC}.
     * @param value DatasetType
     */
    public void setType(final DatasetType value) {

        this.type = value;
    }

    /**
     * Defines a Chart Series
     * Get the seriesDefinitions Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.seriesDefinitions Collection<SeriesDefinitionVO>
     */
    public Collection<SeriesDefinitionVO> getSeriesDefinitions() {

        if (this.seriesDefinitions == null) {

            this.seriesDefinitions = new ArrayList<>();
        }
        return this.seriesDefinitions;
    }

    /**
     * Sets the seriesDefinitions
     * @param value Collection<SeriesDefinitionVO>
     */
    public void setSeriesDefinitions(Collection<SeriesDefinitionVO> value) {

        this.seriesDefinitions = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("DatasetDefinitionVO [type=");
        builder.append(this.type);
        builder.append(", seriesDefinitions=");
        builder.append(this.seriesDefinitions);
        builder.append("]");

        return builder.toString();
    }

}
