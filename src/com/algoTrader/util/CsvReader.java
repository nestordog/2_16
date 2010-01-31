package com.algoTrader.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;

public class CsvReader {

    private static String dataSet = PropertiesUtil.getProperty("simulation.dataSet");

    private static CellProcessor[] processor = new CellProcessor[] {
        new ParseDate(),
        new ParseBigDecimal(),
        new ParseDate(),
        new ParseInt(),
        new ParseInt(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseInt(),
        new ParseInt(),
        new ParseBigDecimal()};

    private String[] header;
    private CsvBeanReader reader;

    public CsvReader(String symbol ) throws SuperCSVException, IOException  {

        File file = new File("results/tickdata/" + dataSet + "/" + symbol + ".csv");
        Reader inFile = new FileReader(file);
        reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        header = reader.getCSVHeader(true);
    }

     private static class ParseDate extends CellProcessorAdaptor {

             public ParseDate() {
                super();
            }

            public ParseDate(final LongCellProcessor next) {
                super(next);
            }

            public Object execute(final Object value, final CSVContext context) throws NumberFormatException {

                Date date = new Date(Long.parseLong((String)value));

                /*
                SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", new Locale("en"));

                Date result = null;
                try {
                    result = format.parse((String)value);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return next.execute(result, context);
                */

                return next.execute(date, context);
            }
        }

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvReader csvReader = new CsvReader("CH00099808949");

        Tick tick;
        while ((tick = csvReader.readTick()) != null) {
                System.out.print(tick);
        }
    }

    public Tick readTick() throws SuperCSVReflectionException, IOException {

          Tick tick;
          if ( (tick = reader.read(TickImpl.class, header, processor)) != null) {
              return tick;
          } else {
              reader.close();
              return null;
          }
    }
}
