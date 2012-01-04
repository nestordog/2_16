package com.algoTrader.util.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Tick;

public class CsvTickWriter {

    //@formatter:off
    private static String[] header = new String[] {
        "dateTime",
        "last",
        "lastDateTime",
        "volBid",
        "volAsk",
        "bid",
        "ask",
        "vol",
        "openIntrest",
        "settlement"
    };

    private static CellProcessor[] processor = new CellProcessor[] {
        new DateConverter(), //dateTime
        new ConvertNullTo(""), //last
        new DateConverter(), //lastDateTime
        null, //volBid
        null, //volAsk
        null, //bid
        null, //ask
        null, //vol
        null, //openIntrest
        new ConvertNullTo("") //settlement
    };
    //@formatter:on

    private static String dataSet = ServiceLocator.instance().getConfiguration().getDataSet();

    private CsvBeanWriter writer;

    public CsvTickWriter(File file) throws SuperCSVException, IOException {

        boolean exists = file.exists();

        this.writer = new CsvBeanWriter(new FileWriter(file, true), CsvPreference.EXCEL_PREFERENCE);

        if (!exists) {
            this.writer.writeHeader(header);
        }
    }

    public CsvTickWriter(String symbol) throws SuperCSVException, IOException {

        this(new File("results/tickdata/" + dataSet + "/" + symbol + ".csv"));
    }

    private static class DateConverter extends CellProcessorAdaptor {

        public DateConverter() {
            super();
        }

        @Override
        public Object execute(final Object value, final CSVContext context) throws NumberFormatException {
            if (value == null) {
                return "";
            }
            final Date date = (Date) value;
            Long result = Long.valueOf(date.getTime());
            return this.next.execute(result, context);
        }
    }

    public void write(Tick tick) throws SuperCSVReflectionException, IOException {

        this.writer.write(tick, header, processor);
    }

    public void close() throws IOException {

        this.writer.close();
    }
}
