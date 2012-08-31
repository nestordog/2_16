package com.algoTrader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    public static List<String> unzip(String fileName, boolean delete) {

        List<String> fileNames = new ArrayList<String>();

        try {
            int BUFFER = 2048;
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                byte data[] = new byte[BUFFER];
                String entryFileName = file.getParent() + File.separator + entry.getName();
                fileNames.add(entryFileName);

                FileOutputStream fos = new FileOutputStream(entryFileName);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                int count;
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
            }

            zis.close();

            if (delete) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNames;
    }
}
