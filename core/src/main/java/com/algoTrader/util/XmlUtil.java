package com.algoTrader.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.algoTrader.ServiceLocator;

public class XmlUtil {

    private static boolean saveToFile = ServiceLocator.instance().getConfiguration().getBoolean("misc.saveToFile");

    private static Logger logger = MyLogger.getLogger(XmlUtil.class.getName());

    public static void saveDocumentToFile(Document node, String fileName, String directory) {

        if (!saveToFile) {
            return;
        }

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
            DOMSource source = new DOMSource(node);
            OutputStream out = new FileOutputStream("files" + File.separator + directory + File.separator + fileName);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.close();
        } catch (Exception ex) {
            logger.warn(fileName + " could not be written to the file (" + ex.getClass().getName() + ")");
        }
    }
}
