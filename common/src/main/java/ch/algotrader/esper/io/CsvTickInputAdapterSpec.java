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
package ch.algotrader.esper.io;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

import ch.algotrader.util.CustomDate;

/**
 * A {@link CSVInputAdapterSpec} used to input {@link ch.algotrader.entity.marketData.Tick Ticks}.
 * Will use {@code dateTime} as {@code timestampColumn}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvTickInputAdapterSpec extends CSVInputAdapterSpec {

    private File file;

    public CsvTickInputAdapterSpec(File file) {

        super(new AdapterInputSource(file), "RawTick");

        this.file = file;

        //@formatter:off
        String[] tickPropertyOrder = new String[] {
                "dateTime",
                "last",
                "lastDateTime",
                "volBid",
                "volAsk",
                "bid",
                "ask",
                "vol",
                "security"};
        //@formatter:on

        setPropertyOrder(tickPropertyOrder);

        Map<String, Object> tickPropertyTypes = new HashMap<String, Object>();

        tickPropertyTypes.put("dateTime", CustomDate.class);
        tickPropertyTypes.put("last", BigDecimal.class);
        tickPropertyTypes.put("lastDateTime", CustomDate.class);
        tickPropertyTypes.put("volBid", int.class);
        tickPropertyTypes.put("volAsk", int.class);
        tickPropertyTypes.put("bid", BigDecimal.class);
        tickPropertyTypes.put("ask", BigDecimal.class);
        tickPropertyTypes.put("vol", int.class);
        tickPropertyTypes.put("security", String.class);

        setPropertyTypes(tickPropertyTypes);

        setTimestampColumn("dateTime");

        setUsingExternalTimer(true);
    }

    public File getFile() {

        return this.file;
    }
}
