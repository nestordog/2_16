package com.algoTrader.esper.io;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.algoTrader.enumeration.Duration;
import com.algoTrader.util.CustomDate;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class CsvBarInputAdapterSpec extends CSVInputAdapterSpec {

    private File file;
    private Duration barSize;

    public CsvBarInputAdapterSpec(File file, Duration barSize) {

        super(new AdapterInputSource(file), "RawBar");

        this.file = file;
        this.barSize = barSize;

        //@formatter:off
        String[] barPropertyOrder = new String[] {
                "dateTime",
                "open",
                "high",
                "low",
                "close",
                "adjClose",
                "vol",
                "openInterest" };
        //@formatter:on

        setPropertyOrder(barPropertyOrder);

        Map<String, Object> barPropertyTypes = new HashMap<String, Object>();

        barPropertyTypes.put("dateTime", CustomDate.class);
        barPropertyTypes.put("open", BigDecimal.class);
        barPropertyTypes.put("high", BigDecimal.class);
        barPropertyTypes.put("low", BigDecimal.class);
        barPropertyTypes.put("close", BigDecimal.class);
        barPropertyTypes.put("adjClose", BigDecimal.class);
        barPropertyTypes.put("vol", int.class);
        barPropertyTypes.put("openInterest", int.class);

        setPropertyTypes(barPropertyTypes);

        setTimestampColumn("dateTime");

        setUsingExternalTimer(true);
    }

    public File getFile() {

        return this.file;
    }

    public Duration getBarSize() {
        return this.barSize;
    }
}
