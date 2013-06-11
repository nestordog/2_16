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
package ch.algotrader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.algotrader.util.collection.Pair;

/**
 * Provides methods for unziping of Zip-Files.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ZipUtil {

    private static Logger logger = MyLogger.getLogger(ZipUtil.class.getName());

    /**
     * unzipes a Zip-File specified by {@code fileName} to the same directory.
     * If {@code delete} is true, the original Zip-file will be deleted.
     */
    public static List<String> unzip(String fileName, boolean delete) {

        List<String> fileNames = new ArrayList<String>();

        try {
            int BUFFER = 2048;
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                String entryFileName = file.getParent() + File.separator + entry.getName();
                fileNames.add(entryFileName);

                FileOutputStream fos = new FileOutputStream(entryFileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);

                IOUtils.copy(zis, bos);

                bos.flush();
                bos.close();
            }

            zis.close();

            if (delete) {
                file.delete();
            }

        } catch (Exception e) {
            logger.error("problem unzipping", e);
        }

        return fileNames;
    }

    /**
     * Unzipes a Zip-File specified as a byte[] and returns a List of Pairs containing the filename and the File content.
     */
    public static List<Pair<String, byte[]>> unzip(byte[] data) {

        List<Pair<String, byte[]>> entries = new ArrayList<Pair<String, byte[]>>();

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ZipInputStream zis = new ZipInputStream(bis);

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(zis, bos);

                entries.add(new Pair<String, byte[]>(entry.getName(), bos.toByteArray()));

                bos.flush();
                bos.close();
            }

            zis.close();

        } catch (Exception e) {
            logger.error("problem unzipping", e);
        }

        return entries;
    }
}
