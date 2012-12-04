package com.algoTrader.util;

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

import com.algoTrader.util.collection.Pair;

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
            e.printStackTrace();
        }

        return fileNames;
    }

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
            e.printStackTrace();
        }

        return entries;
    }
}
