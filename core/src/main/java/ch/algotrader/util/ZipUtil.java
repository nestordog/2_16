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
package ch.algotrader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import ch.algotrader.util.collection.Pair;

/**
 * Provides methods for unziping of Zip-Files.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ZipUtil {

    private static final int BUFFER = 2048;

    /**
     * unzipes a Zip-File specified by {@code fileName} to the same directory.
     * If {@code delete} is true, the original Zip-file will be deleted.
     * @throws IOException
     */
    public static List<String> unzip(String fileName, boolean delete) throws IOException {

        File file = new File(fileName);

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            ZipEntry entry;
            List<String> fileNames = new ArrayList<>();
            while ((entry = zis.getNextEntry()) != null) {

                String entryFileName = file.getParent() + File.separator + entry.getName();
                fileNames.add(entryFileName);

                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFileName), BUFFER)) {
                    IOUtils.copy(zis, bos);
                }
            }

            if (delete) {
                file.delete();
            }

            return fileNames;

        }
    }

    /**
     * Unzipes a Zip-File specified as a byte[] and returns a List of Pairs containing the filename and the File content.
     * @throws IOException
     */
    public static List<Pair<String, byte[]>> unzip(byte[] data) throws IOException {

        List<Pair<String, byte[]>> entries = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    IOUtils.copy(zis, bos);
                    entries.add(new Pair<>(entry.getName(), bos.toByteArray()));
                }
            }

            return entries;

        }
    }
}
