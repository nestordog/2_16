package com.algoTrader.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;

public class CsvWriter {

    private static String[] header = new String[] { "dateTime", "last", "lastDateTime", "volBid", "volAsk", "bid", "ask", "vol", "openIntrest", "settlement" };
    private static CellProcessor[] processor = new CellProcessor[] { new DateConverter(), null, new DateConverter(), null, null, null, null, null, null, null };

    private CsvBeanWriter writer;

    public CsvWriter(String symbol ) throws SuperCSVException, IOException  {

        File file = new File("results/tickdata/" + symbol + ".csv");
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

            public DateConverter(final LongCellProcessor next) {
                super(next);
            }

            public Object execute(final Object value, final CSVContext context) throws NumberFormatException {
                final Date date = (Date) value;
                Long result = Long.valueOf(date.getTime());
                return next.execute(result, context);
            }
        }

    public static void main(String[] args) throws SuperCSVException, IOException {


        Tick tick = new TickImpl();
        tick.setDateTime(new Date());
        tick.setLast(getBigDecimal(18.4));
        tick.setLastDateTime(new Date());
        tick.setVolBid(50);
        tick.setVolAsk(100);
        tick.setBid(getBigDecimal(16.5));
        tick.setAsk(getBigDecimal(17.4));
        tick.setVol(1000);
        tick.setOpenIntrest(16000);
        tick.setSettlement(getBigDecimal(20));

        CsvWriter csvWriter = new CsvWriter("osmi");

        csvWriter.writeTick(tick);

        csvWriter.close();
    }

    public void writeTick(Tick tick) throws SuperCSVReflectionException, IOException {

        writer.write(tick, header, processor);
    }

    public void close() throws IOException {

        writer.close();
    }

    private static BigDecimal getBigDecimal(double value) {

        BigDecimal decimal = new BigDecimal(value);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
