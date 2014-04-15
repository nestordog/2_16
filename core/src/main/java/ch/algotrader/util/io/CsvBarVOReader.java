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
package ch.algotrader.util.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import ch.algotrader.util.ConfigurationUtil;
import ch.algotrader.vo.BarVO;

/**
 * SuperCSV Reader that reads {@link BarVO BarVOs} from the specified CSV-File.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvBarVOReader {

    private static String dataSet = ConfigurationUtil.getString("dataSet");

    //@formatter:off
    private static CellProcessor[] processor = new CellProcessor[] {
        new ParseDate(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseBigDecimal()};
    //@formatter:on

    private String[] header;
    private CsvBeanReader reader;

    public CsvBarVOReader(String symbol) throws IOException {

        File file = new File("files" + File.separator + "tickdata" + File.separator + dataSet + File.separator + symbol + ".csv");
        Reader inFile = new FileReader(file);
        this.reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        this.header = this.reader.getHeader(true);
    }

    private static class ParseDate extends CellProcessorAdaptor {

        public ParseDate() {
            super();
        }

        @Override
        public Object execute(final Object value, final CsvContext context) {

            Date date = new Date(Long.parseLong((String) value));

            return this.next.execute(date, context);
        }
    }

    public BarVO readBarVO() throws IOException {

        BarVO bar;
        if ((bar = this.reader.read(BarVO.class, this.header, processor)) != null) {
            return bar;
        } else {
            this.reader.close();
            return null;
        }
    }
}
