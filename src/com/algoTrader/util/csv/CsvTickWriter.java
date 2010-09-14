package com.algoTrader.util.csv;

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

import com.algoTrader.entity.Tick;
import com.algoTrader.util.PropertiesUtil;

public class CsvTickWriter {

    private static String[] header = new String[] { "dateTime", "last", "lastDateTime", "volBid", "volAsk", "bid", "ask", "vol", "openIntrest", "settlement" };
    private static CellProcessor[] processor = new CellProcessor[] { new DateConverter(), new ConvertNullTo(""), new DateConverter(), null, null, null, null, null, null, null };
    private static String dataSet = PropertiesUtil.getProperty("strategie.dataSet");

    private CsvBeanWriter writer;

    public CsvTickWriter(String symbol ) throws SuperCSVException, IOException  {

        File file = new File("results/tickdata/" + dataSet + "/" + symbol + ".csv");
        boolean exists = file.exists();

        this.writer = new CsvBeanWriter(new FileWriter(file, true), CsvPreference.EXCEL_PREFERENCE);

        if (!exists) {
            this.writer.writeHeader(header);
        }
    }

     private static class DateConverter extends CellProcessorAdaptor {

             public DateConverter() {
                super();
            }

            public Object execute(final Object value, final CSVContext context) throws NumberFormatException {
                if (value == null) return "";
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
