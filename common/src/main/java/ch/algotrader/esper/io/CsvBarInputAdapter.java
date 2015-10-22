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
package ch.algotrader.esper.io;

import ch.algotrader.enumeration.Duration;

import com.espertech.esper.client.EPException;
import com.espertech.esperio.SendableBeanEvent;
import com.espertech.esperio.SendableEvent;
import com.espertech.esperio.csv.CSVInputAdapter;

/**
 *  A {@link CSVInputAdapter} used to input {@link ch.algotrader.entity.marketData.Bar Bars}.
 *  Will retrieve the {@code isin} for the {@code fileName} and the {@code barSize} from the {@link com.espertech.esperio.csv.CSVInputAdapterSpec}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CsvBarInputAdapter extends CSVInputAdapter {

    private final CsvBarInputAdapterSpec spec;

    public CsvBarInputAdapter(CsvBarInputAdapterSpec spec) {

        super(null, spec);
        this.spec = spec;
    }

    @Override
    public SendableEvent read() throws EPException {

        SendableBeanEvent event = (SendableBeanEvent) super.read();

        if (event != null && event.getBeanToSend() instanceof RawBarVO) {

            RawBarVO bar = (RawBarVO) event.getBeanToSend();

            if (bar.getSecurity() == null) {

                String fileName = this.spec.getFile().getName();
                int idx = fileName.lastIndexOf(".");
                String security = fileName.substring(0, idx);
                bar.setSecurity(security);
            }

            Duration barSize = this.spec.getBarSize();
            bar.setBarSize(barSize);
        }
        return event;
    }
}
