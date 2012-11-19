package com.algoTrader.service.rbs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

public class CsvRBSPositionReader {

    //@formatter:off
    private static CellProcessor[] processor = new CellProcessor[] {
        new ParseInt(),
        new ParseLong(),
        new StrNotNullOrEmpty(),
        new ParseDate("dd/MM/yyyy"),
        new ParseBigDecimal(),
        new StrNotNullOrEmpty(),
        new StrNotNullOrEmpty(),
        new ParseBigDecimal(),
        new StrNotNullOrEmpty(),
        new ParseBigDecimal(),
        new ParseBigDecimal(),
        new ParseDate("dd/MM/yyyy"),
        new ParseInt(),
        new ParseInt(),
        new StrNotNullOrEmpty()
    };
    //@formatter:on

    public static List<Map<String, ? super Object>> readPositions(byte[] data) throws SuperCSVReflectionException, IOException {

        Reader isr = new InputStreamReader(new ByteArrayInputStream(data));
        CsvMapReader reader = new CsvMapReader(isr, CsvPreference.EXCEL_PREFERENCE);
        String[] header = reader.getCSVHeader(true);

        List<Map<String, ? super Object>> list = new ArrayList<Map<String, ? super Object>>();

        Map<String, ? super Object> position;
        while ((position = reader.read(header, processor)) != null) {
            list.add(position);
        }

        reader.close();

        return list;
    }
}
