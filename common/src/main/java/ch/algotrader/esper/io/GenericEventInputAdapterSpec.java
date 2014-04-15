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

import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

/**
 * A {@link CSVInputAdapterSpec} used to input arbitrary events from  a file.
 * Will use {@code time} as {@code timestampColumn}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericEventInputAdapterSpec extends CSVInputAdapterSpec {

    public GenericEventInputAdapterSpec(File file, String eventTypeName) {

        super(null, null);

        setAdapterInputSource(new AdapterInputSource(file));
        seteventTypeName(eventTypeName);
        setTimestampColumn("time");
        setUsingExternalTimer(true);
    }
}
