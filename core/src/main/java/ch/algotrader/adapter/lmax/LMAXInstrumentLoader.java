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
package ch.algotrader.adapter.lmax;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
class LMAXInstrumentLoader {

    private static final String[] HEADERS = new String[] {
            "name", "id", "symbol", "contractMultiplier", "tickSize", "tickValue", "effectiveDate", "expiryDate", "quotedCurrency", null
    };

    private CellProcessor[] getProcessors() {
        return new CellProcessor[] {
                new Optional(),
                new UniqueHashCode(),
                new UniqueHashCode(),
                new ParseBigDecimal(),
                new ParseBigDecimal(),
                new ParseBigDecimal(),
                new Optional(new ParseDate("dd/MM/yyyy")), //TODO: ParseDate always uses default time zone and is therefore irreparably broken
                new Optional(new ParseDate("dd/MM/yyyy HH:mm")), //TODO: effectiveDate and expiryDate are currently not used
                new Optional(),
                null
        };
    }

    List<LMAXInstrumentDef> load(final Reader reader) throws IOException {

        CsvBeanReader parser = new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE);
        parser.getHeader(true);

        List<LMAXInstrumentDef> list = new ArrayList<LMAXInstrumentDef>();
        LMAXInstrumentDef bean;
        while ((bean = parser.read(LMAXInstrumentDef.class, HEADERS, getProcessors())) != null) {
            list.add(bean);
        }
        return list;
    }

}
