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
package ch.algotrader.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;

import ch.algotrader.entity.marketData.Tick;

/**
 * SuperCSV based utility class, that merges Tick CSV files.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvMerger {

    public static void main(String[] args) throws IOException {

        merge(args[0], args[1], args[2]);
    }

    /**
     * merges all files from directories "files/tickdata/[a]" and "files/tickdata/[b]" into "files/tickdata/[c]".
     * If a file is contained in both source directories it will be merged by timestamp.
     */
    public static void merge(String a, String b, String c) throws IOException {

        File aDir = new File("files" + File.separator + "tickdata" + File.separator + a + File.separator);
        File bDir = new File("files" + File.separator + "tickdata" + File.separator + b + File.separator);
        File cDir = new File("files" + File.separator + "tickdata" + File.separator + c + File.separator);

        if (!cDir.exists()) {
            cDir.mkdir();
        }

        Collection<String> aNames = CollectionUtils.collect(Arrays.asList(aDir.listFiles()), new Transformer<File, String>() {
            @Override
            public String transform(File file) {
                return FilenameUtils.getName(file.getName());
            }});

        Collection<String> bNames = CollectionUtils.collect(Arrays.asList(bDir.listFiles()), new Transformer<File, String>() {
            @Override
            public String transform(File file) {
                return FilenameUtils.getName(file.getName());
            }});

        for (String fileName : new HashSet<String>(CollectionUtils.union(aNames, bNames))) {

            if (aNames.contains(fileName) && bNames.contains(fileName)) {

                CsvTickReader aReader = new CsvTickReader(new File(aDir.getPath() + File.separator + fileName));
                CsvTickReader bReader = new CsvTickReader(new File(bDir.getPath() + File.separator + fileName));
                CsvTickWriter csvWriter = new CsvTickWriter(new File(cDir.getPath() + File.separator + fileName));

                Tick aTick = aReader.readTick();
                Tick bTick = bReader.readTick();
                Tick lastTick = null;
                do {

                    // consider the earliest tick from both files
                    Tick newTick = null;
                    if (aTick != null & bTick == null) {
                        newTick = aTick;
                        aTick = aReader.readTick();
                    } else if (bTick != null & aTick == null) {
                        newTick = bTick;
                        bTick = bReader.readTick();
                    } else if (aTick.getDateTime().getTime() < bTick.getDateTime().getTime()) {
                        newTick = aTick;
                        aTick = aReader.readTick();
                    } else if (bTick.getDateTime().getTime() < aTick.getDateTime().getTime()) {
                        newTick = bTick;
                        bTick = bReader.readTick();
                    } else {
                        newTick = aTick;
                        aTick = aReader.readTick();
                        bTick = bReader.readTick();
                    }

                    // round to one minute
                    newTick.setDateTime(DateUtils.round(newTick.getDateTime(), Calendar.MINUTE));

                    // do not write twice for the same minute
                    if (lastTick == null || !lastTick.getDateTime().equals(newTick.getDateTime())) {
                        csvWriter.write(newTick);
                    }

                    lastTick = newTick;

                } while (aTick != null || bTick != null);

                csvWriter.close();

            } else if (aNames.contains(fileName)) {
                FileUtils.copyFileToDirectory(new File(aDir.getPath() + File.separator + fileName), cDir);
            } else if (bNames.contains(fileName)) {
                FileUtils.copyFileToDirectory(new File(bDir.getPath() + File.separator + fileName), cDir);
            }
        }
    }
}
