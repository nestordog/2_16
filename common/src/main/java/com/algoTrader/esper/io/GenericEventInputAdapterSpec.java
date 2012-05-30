package com.algoTrader.esper.io;

import java.io.File;

import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

public class GenericEventInputAdapterSpec extends CSVInputAdapterSpec {

    public GenericEventInputAdapterSpec(File file, String eventTypeName) {

        super(null, null);

        setAdapterInputSource(new AdapterInputSource(file));
        seteventTypeName(eventTypeName);
        setTimestampColumn("dateTime");
        setUsingExternalTimer(true);
    }
}
