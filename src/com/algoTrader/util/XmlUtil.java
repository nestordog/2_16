package com.algoTrader.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class XmlUtil {

    private static boolean saveToFile = new Boolean(PropertiesUtil.getProperty("saveToFile")).booleanValue();

    private static Logger logger = MyLogger.getLogger(XmlUtil.class.getName());

    public static void saveDocumentToFile(Document node, String fileName, String directory, boolean force) {

        if (!force && !saveToFile) return;

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
            DOMSource source = new DOMSource(node);
            OutputStream out = new FileOutputStream(directory + fileName);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.close();
        } catch (IllegalArgumentException ex) {
            logger.warn(fileName + " could not be written to the file");
        } catch (TransformerFactoryConfigurationError ex) {
            logger.warn(fileName + " could not be written to the file");
        } catch (TransformerException ex) {
            logger.warn(fileName + " could not be written to the file");
        } catch (IOException ex) {
            logger.warn(fileName + " could not be written to the file");
        }
    }
}
