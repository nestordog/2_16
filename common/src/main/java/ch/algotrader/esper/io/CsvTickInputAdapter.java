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

import org.apache.commons.lang.StringUtils;

import ch.algotrader.vo.RawTickVO;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.SendableBeanEvent;
import com.espertech.esperio.SendableEvent;
import com.espertech.esperio.csv.CSVInputAdapter;

/**
 * A {@link CSVInputAdapter} used to input {@link ch.algotrader.entity.marketData.Tick Ticks}.
 * Will retrieve the {@code isin} for the {@code fileName}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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

            if (tick.getSecurity() == null) {

                String fileName = this.spec.getFile().getName();
                int idx = fileName.lastIndexOf(".");
                String security = fileName.substring(0, idx);
                tick.setSecurity(security);
            }
        }
        return event;
    }
}
