/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util.diff.reader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

import ch.algotrader.util.diff.define.CsvDefinition;

/**
 * Reader returning the lines from an in-memory linked-list. Used as sub-reader
 * returned by {@link BufferedReader#readBufferAsReader()}.
 */
public class LinkedListReader implements CsvReader {

    private final CsvReader baseReader;
    private final int lastLine;
    private final LinkedList<CsvLine> lines;

    public LinkedListReader(CsvReader baseReader, int lineOffset, CsvLine line) {
        this(baseReader, lineOffset, Collections.singletonList(line));
    }
    public LinkedListReader(CsvReader baseReader, int lineOffset, Collection< ? extends CsvLine> lines) {
        this.baseReader = Objects.requireNonNull(baseReader, "baseReader cannot be null");
        this.lastLine = lineOffset + lines.size();
        this.lines = new LinkedList<CsvLine>(lines);
    }

    @Override
    public CsvDefinition getCsvDefinition() {
        return baseReader.getCsvDefinition();
    }

    @Override
    public File getFile() {
        return baseReader.getFile();
    }

    @Override
    public java.io.BufferedReader getReader() {
        return baseReader.getReader();
    }

    @Override
    public int getLineIndex() {
        return lastLine - lines.size();
    }

    public int getLineCount() {
        return lines.size();
    }

    @Override
    public CsvLine readLine() throws IOException {
        return lines.isEmpty() ? CsvLine.getEofLine(this) : lines.removeFirst();
    }

}
