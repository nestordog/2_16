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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import ch.algotrader.ServiceLocator;

/**
 * Provides XML-persistence methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class XmlUtil {

    private static boolean saveToFile = ServiceLocator.instance().getConfiguration().getBoolean("misc.saveToFile");

    /**
     * Writes a {@link Document} to a textFile specified by {@code fileName} and {@code directory}
     * @throws TransformerException
     * @throws IOException
     */
    public static void saveDocumentToFile(Document document, String fileName, String directory) throws TransformerException, IOException {

        if (!saveToFile) {
            return;
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
        DOMSource source = new DOMSource(document);
        OutputStream out = new FileOutputStream("files" + File.separator + directory + File.separator + fileName);

        try {
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } finally {
            out.close();
        }
    }
}
