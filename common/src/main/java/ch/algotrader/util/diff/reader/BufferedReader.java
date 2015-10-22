/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.util.diff.reader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ch.algotrader.util.diff.define.CsvDefinition;

/**
 * Buffered reader to allow re-reading of the same line (look-ahead feature).
 * The buffered lines can also be returned as a sub-reader.
 */
public class BufferedReader implements CsvReader {

    private final CsvReader delegate;
    private final LinkedList<CsvLine> buf = new LinkedList<>();

    public BufferedReader(CsvReader delegate) {
        this.delegate = delegate;
    }

    @Override
    public CsvDefinition getCsvDefinition() {
        return delegate.getCsvDefinition();
    }

    @Override
    public File getFile() {
        return delegate.getFile();
    }

    @Override
    public java.io.BufferedReader getReader() {
        return delegate.getReader();
    }

    public CsvReader getDelegate() {
        return delegate;
    }

    @Override
    public int getLineIndex() {
        return delegate.getLineIndex() - buf.size();
    }

    @Override
    public CsvLine readLine() throws IOException {
        if (buf.isEmpty()) {
            return delegate.readLine();
        }
        return buf.removeFirst();
    }

    public CsvLine readLineIntoBuffer() throws IOException {
        final CsvLine line = delegate.readLine();
        if (line.isValid()) {
            buf.add(line);
        }
        return line;
    }

    public LinkedList<CsvLine> readBuffer() {
        return readBuffer(buf.size());
    }
    public LinkedList<CsvLine> readBuffer(int count) {
        final List<CsvLine> subList = count == buf.size() ? buf : buf.subList(0, count);
        final LinkedList<CsvLine> result = new LinkedList<>(subList);
        subList.clear();
        return result;
    }

    public CsvLine getFirstLineInBuffer() {
        return buf.getFirst();
    }

    public CsvLine getLastLineInBuffer() {
        return buf.getLast();
    }

    public LinkedListReader readBufferAsReader() {
        return readBufferAsReader(buf.size());
    }
    public LinkedListReader readBufferAsReader(int count) {
        return new LinkedListReader(this, getLineIndex(), readBuffer(count));
    }

    public int getBufferSize() {
        return buf.size();
    }

    public void clearBuffer() {
        buf.clear();
    }
}
