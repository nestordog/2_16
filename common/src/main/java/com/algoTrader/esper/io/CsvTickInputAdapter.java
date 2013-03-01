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
package com.algoTrader.esper.io;

import com.algoTrader.vo.RawTickVO;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.SendableBeanEvent;
import com.espertech.esperio.SendableEvent;
import com.espertech.esperio.csv.CSVInputAdapter;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvTickInputAdapter extends CSVInputAdapter {

    private CsvTickInputAdapterSpec spec;

    public CsvTickInputAdapter(EPServiceProvider epService, CsvTickInputAdapterSpec spec) {

        super(epService, spec);
        this.spec = spec;
    }

    @Override
    public SendableEvent read() throws EPException {
        SendableBeanEvent event = (SendableBeanEvent) super.read();

        if (event != null && event.getBeanToSend() instanceof RawTickVO) {

            RawTickVO tick = (RawTickVO) event.getBeanToSend();
            String isin = this.spec.getFile().getName().split("\\.")[0];
            tick.setIsin(isin);
        }
        return event;
    }
}
