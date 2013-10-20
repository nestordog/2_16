/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.Token;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import ch.algotrader.vo.IVolVO;

/**
 * SuperCSV Reader that reads {@link IVolVO IVolVOs} from the specified CSV-File.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvIVolReader {

    //@formatter:off
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
    //@formatter:on

    private CsvBeanReader reader;

    public CsvIVolReader(String fileName) throws IOException {

        File file = new File("files" + File.separator + "iVol" + File.separator + fileName);
        Reader inFile = new FileReader(file);
        this.reader = new CsvBeanReader(inFile, CsvPreference.EXCEL_PREFERENCE);
        this.reader.getHeader(true);
    }

    public IVolVO readHloc() throws IOException {

        IVolVO ivol;
        if ((ivol = this.reader.read(IVolVO.class, this.header, processor)) != null) {
            return ivol;
        } else {
            this.reader.close();
            return null;
        }
    }
}
