package com.algoTrader.util.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.Token;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.algoTrader.vo.IVolVO;

public class CsvIVolReader {

    private static CellProcessor[] processor = new CellProcessor[] {
        null,
        null,
        new ParseDate("MM/dd/yy"),
        new Token("N/A", null, new ParseBigDecimal()),
        null,
        new ParseDate("MM/dd/yy"),
        new ParseBigDecimal(),
        null,
        new Token("N/A", null, new ParseBigDecimal()),
        new Token("N/A", null, new ParseBigDecimal()),
        new Token("N/A", 0, new ParseInt()),
        new Token("N/A", 0, new ParseInt()),
        new Token("N/A", null, new ParseBigDecimal())
    };

    private String[] header = {
        "symbol",
        "exchange",
        "date",
        "adjustedStockClosePrice",
        "optionSymbol",
        "expiration",
        "strike",
        "type",
        "ask",
        "bid",
        "volume",
        "openIntrest",
        "unadjustedStockPrice"};

    private CsvBeanReader reader;

    public CsvIVolReader(String fileName) throws SuperCSVException, IOException {

        File file = new File("results/iVol/" + fileName);
        Reader inFile = new FileReader(file);
        this.reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        this.reader.getCSVHeader(true);
    }

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvIVolReader csvReader = new CsvIVolReader("data_download_01.csv");

        IVolVO iVol;
        while ((iVol = csvReader.readHloc()) != null) {
            System.out.println(iVol);
        }
    }

    public IVolVO readHloc() throws SuperCSVReflectionException, IOException {

        IVolVO ivol;
        if ((ivol = this.reader.read(IVolVO.class, this.header, processor)) != null) {
            return ivol;
        } else {
            this.reader.close();
            return null;
        }
    }
}
