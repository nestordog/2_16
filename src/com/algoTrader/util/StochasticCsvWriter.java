package com.algoTrader.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.supercsv.exception.SuperCSVException;
import org.supercsv.exception.SuperCSVReflectionException;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

public class StochasticCsvWriter {

    private static String[] header = new String[] { "dateTime", "kFast", "kSlow", "dSlow" };

    private CsvMapWriter writer;

    public StochasticCsvWriter(String symbol) throws SuperCSVException, IOException  {

        File file = new File("results/stochastic/" + symbol + ".csv");
        boolean exists = file.exists();

        writer = new CsvMapWriter(new FileWriter(file, true), CsvPreference.EXCEL_PREFERENCE);

        if (!exists) {
            writer.writeHeader(header);
        }
    }

    public void write(Map<String, Number> stochastic) throws SuperCSVReflectionException, IOException {

        writer.write(stochastic, header);
    }

    public void close() throws IOException {

        writer.close();
    }
}
