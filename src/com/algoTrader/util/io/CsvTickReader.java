package com.algoTrader.util.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.Token;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.PropertiesUtil;

public class CsvTickReader {

    private static String dataSet = PropertiesUtil.getProperty("strategie.dataSet");

    private static CellProcessor[] processor = new CellProcessor[] {
        new ParseDate(),
        new Token("", new BigDecimal(0), new ParseBigDecimal()),
        new ParseDate(),
        new ParseInt(),
        new ParseInt(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseInt(),
        new ParseInt(),
        new ParseBigDecimal()
    };

    private String[] header;
    private CsvBeanReader reader;

    public CsvTickReader(String symbol) throws SuperCSVException, IOException {

        File file = new File("results/tickdata/" + dataSet + "/" + symbol + ".csv");
        Reader inFile = new FileReader(file);
        this.reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        this.header = this.reader.getCSVHeader(true);
    }

    private static class ParseDate extends CellProcessorAdaptor {

        public ParseDate() {
            super();
        }

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

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvTickReader csvReader = new CsvTickReader("CH00099808949");

        Tick tick;
        while ((tick = csvReader.readTick()) != null) {
            System.out.print(tick);
        }
    }

    public Tick readTick() throws SuperCSVReflectionException, IOException {

        Tick tick;
        if ((tick = this.reader.read(TickImpl.class, this.header, processor)) != null) {
            return tick;
        } else {
            this.reader.close();
            return null;
        }
    }
}
