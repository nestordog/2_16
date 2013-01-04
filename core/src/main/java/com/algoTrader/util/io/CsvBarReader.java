package com.algoTrader.util.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.BarImpl;

public class CsvBarReader {

    private static String dataSet = "";//ServiceLocator.instance().getConfiguration().getDataSet();

    //@formatter:off
    private static CellProcessor[] processor = new CellProcessor[] {
        new ParseDate(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseInt(),
        new ParseInt(),
        new ParseBigDecimal()
    };
    //@formatter:on

    private String[] header;
    private CsvBeanReader reader;

    public CsvBarReader(File file) throws SuperCSVException, IOException {

        Reader inFile = new FileReader(file);
        this.reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        this.header = this.reader.getCSVHeader(true);
    }

    public CsvBarReader(String fileName) throws SuperCSVException, IOException {

        this(new File("files" + File.separator + "bardata" + File.separator + dataSet + File.separator + fileName + ".csv"));
    }

    private static class ParseDate extends CellProcessorAdaptor {

        public ParseDate() {
            super();
        }

        @Override
        public Object execute(final Object value, final CSVContext context) throws NumberFormatException {

            Date date;
            if (value == null || "".equals(value)) {
                date = null;
            } else {
                date = new Date(Long.parseLong((String) value));
            }

            return this.next.execute(date, context);
        }
    }

    public Bar readBar() throws SuperCSVReflectionException, IOException {

        Bar bar;
        if ((bar = this.reader.read(BarImpl.class, this.header, processor)) != null) {
            return bar;
        } else {
            this.reader.close();
            return null;
        }
    }
}
