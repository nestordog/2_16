package com.algoTrader.esper.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.vo.HlocVO;

public class CsvHlocReader {

    private static String dataSet = ConfigurationUtil.getBaseConfig().getString("dataSource.dataSet");

    private static CellProcessor[] processor = new CellProcessor[] {
        new ParseDate(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseBigDecimal()};

    private String[] header;
    private CsvBeanReader reader;

    public CsvHlocReader(String symbol) throws SuperCSVException, IOException {

        File file = new File("results/tickdata/" + dataSet + "/" + symbol + ".csv");
        Reader inFile = new FileReader(file);
        this.reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        this.header = this.reader.getCSVHeader(true);
    }

    private static class ParseDate extends CellProcessorAdaptor {

        public ParseDate() {
            super();
        }

        @Override
        public Object execute(final Object value, final CSVContext context) {

            Date date = new Date(Long.parseLong((String) value));

            return this.next.execute(date, context);
        }
    }

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvHlocReader csvReader = new CsvHlocReader("CH0008616382");

        HlocVO hloc;
        while ((hloc = csvReader.readHloc()) != null) {
            System.out.println(hloc);
        }
    }

    public HlocVO readHloc() throws SuperCSVReflectionException, IOException {

        HlocVO hloc;
        if ((hloc = this.reader.read(HlocVO.class, this.header, processor)) != null) {
            return hloc;
        } else {
            this.reader.close();
            return null;
        }
    }
}
