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
package ch.algotrader.adapter.lmax;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.algotrader.util.Consts;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class LMAXInstrumentCodeMapper {

    private final Map<String, String> symbolToCode;
    private final Map<String, String> codeToSymbol;

    public static LMAXInstrumentCodeMapper load() throws IOException {
        ClassLoader cl = LMAXInstrumentCodeMapper.class.getClassLoader();
        URL resource = cl.getResource("lmax/LMAX-Instruments.csv");
        if (resource == null) {
            throw new IOException("LMAX instrument list not found");
        }

        List<LMAXInstrumentDef> instrumentDefs;
        InputStream inputStream = resource.openStream();
        try {
            LMAXInstrumentLoader loader = new LMAXInstrumentLoader();
            instrumentDefs = loader.load(new InputStreamReader(inputStream, Consts.ISO_8859_1));
        } finally {
            inputStream.close();
        }
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>(instrumentDefs.size());
        for (LMAXInstrumentDef instrumentDef: instrumentDefs) {
            map.put(instrumentDef.getSymbol(), instrumentDef.getId());
        }
        return new LMAXInstrumentCodeMapper(map);
    }

    private LMAXInstrumentCodeMapper(final ConcurrentHashMap<String, String> map) {
        this.symbolToCode = new ConcurrentHashMap<String, String>(map.size());
        this.codeToSymbol = new ConcurrentHashMap<String, String>(map.size());
        for (Map.Entry<String, String> entry: map.entrySet()) {
            this.symbolToCode.put(entry.getKey(), entry.getValue());
            this.codeToSymbol.put(entry.getValue(), entry.getKey());
        }
    }

    public String mapToCode(final String symbol) {
        if (symbol == null) {
            return null;
        }
        return symbolToCode.get(symbol);
    }

    public String mapToSymbol(final String code) {
        if (code == null) {
            return null;
        }
        return codeToSymbol.get(code);
    }

}
