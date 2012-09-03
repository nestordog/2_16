package com.algoTrader.util.io;

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
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.marketData.Tick;

public class CsvMerger {

    private static double maxGapDays = 4.0;

    public static void main(String[] args) throws SuperCSVException, IOException {

        File aDir = new File("files" + File.separator + "tickdata" + File.separator + args[0] + File.separator);
        File bDir = new File("files" + File.separator + "tickdata" + File.separator + args[1] + File.separator);
        File cDir = new File("files" + File.separator + "tickdata" + File.separator + args[2] + File.separator);

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
                    } else {
                        System.currentTimeMillis();
                    }

                    // warn if gap is greater than x days
                    if (lastTick != null) {
                        double daysDiff = (double) (newTick.getDateTime().getTime() - lastTick.getDateTime().getTime()) / 86400000;
                        if (daysDiff > maxGapDays) {
                            System.out.println(fileName + " at " + newTick.getDateTime() + " gap of " + daysDiff);
                        }
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
