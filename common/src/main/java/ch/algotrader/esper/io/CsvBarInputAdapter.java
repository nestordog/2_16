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
package ch.algotrader.esper.io;

import ch.algotrader.enumeration.Duration;
import ch.algotrader.vo.RawBarVO;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.SendableBeanEvent;
import com.espertech.esperio.SendableEvent;
import com.espertech.esperio.csv.CSVInputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

/**
 *  A {@link CSVInputAdapter} used to input {@link ch.algotrader.entity.marketData.Bar Bars}.
 *  Will retrieve the {@code isin} for the {@code fileName} and the {@code barSize} from the {@link CSVInputAdapterSpec}.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvBarInputAdapter extends CSVInputAdapter {

    private CsvBarInputAdapterSpec spec;

    public CsvBarInputAdapter(EPServiceProvider epService, CsvBarInputAdapterSpec spec) {

        super(epService, spec);
        this.spec = spec;
    }

    @Override
    public SendableEvent read() throws EPException {

        SendableBeanEvent event = (SendableBeanEvent) super.read();

        if (event != null && event.getBeanToSend() instanceof RawBarVO) {

            RawBarVO bar = (RawBarVO) event.getBeanToSend();

            String fileName = this.spec.getFile().getName().split("\\.")[0];
            bar.setFileName(fileName);

            Duration barSize = this.spec.getBarSize();
            bar.setBarSize(barSize);
        }
        return event;
    }
}
