package com.algoTrader.util.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
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
    private static CellProcessor[] processor = new CellProcessor[] { new DateConverter(), null, new DateConverter(), null, null, null, null, null, null, null };
    private static String dataSet = PropertiesUtil.getProperty("simulation.dataSet");

    private CsvBeanWriter writer;

    public CsvTickWriter(String symbol ) throws SuperCSVException, IOException  {

        File file = new File("results/tickdata/" + dataSet + "/" + symbol + ".csv");
        boolean exists = file.exists();

        writer = new CsvBeanWriter(new FileWriter(file, true), CsvPreference.EXCEL_PREFERENCE);

        if (!exists) {
            writer.writeHeader(header);
        }
    }

     private static class DateConverter extends CellProcessorAdaptor {

             public DateConverter() {
                super();
            }

            public Object execute(final Object value, final CSVContext context) throws NumberFormatException {
                final Date date = (Date) value;
                Long result = Long.valueOf(date.getTime());
                return next.execute(result, context);
            }
        }

    public void write(Tick tick) throws SuperCSVReflectionException, IOException {

        writer.write(tick, header, processor);
    }

    public void close() throws IOException {

        writer.close();
    }
}
