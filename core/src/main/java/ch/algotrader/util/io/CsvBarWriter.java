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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.marketData.Bar;

/**
 * SuperCSV Writer that writes {@link Bar Bars} to the specified CSV-File.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvBarWriter {

    //@formatter:off
    private static final String[] header = new String[] {
        "dateTime",
        "open",
        "high",
        "low",
        "close",
        "vol"
    };

    private static final CellProcessor[] processor = new CellProcessor[] {
        new DateConverter(), //dateTime
        null, //open
        null, //high
        null, //low
        null, //close
        null //vol
    };
    //@formatter:on

    private final CsvBeanWriter writer;

    public CsvBarWriter(File file) throws IOException {

        File parent = file.getParentFile();
        if (!parent.exists()) {
            FileUtils.forceMkdir(parent);
        }

        boolean exists = file.exists();

        this.writer = new CsvBeanWriter(new FileWriter(file, true), CsvPreference.EXCEL_PREFERENCE);

        if (!exists) {
            this.writer.writeHeader(header);
        }
    }

    public CsvBarWriter(String fileName) throws IOException {

        this(new File("files" + File.separator + "bardata" + File.separator + ConfigLocator.instance().getCommonConfig().getDataSet() + File.separator + fileName + ".csv"));
    }

    private static class DateConverter extends CellProcessorAdaptor {

        public DateConverter() {
            super();
        }

        @Override
        public Object execute(final Object value, final CsvContext context) throws NumberFormatException {
            if (value == null) {
                return "";
            }
            final Date date = (Date) value;
            Long result = date.getTime();
            return this.next.execute(result, context);
        }
    }

    public void write(Bar bar) throws IOException {

        this.writer.write(bar, header, processor);
    }

    public void close() throws IOException {

        this.writer.close();
    }
}
