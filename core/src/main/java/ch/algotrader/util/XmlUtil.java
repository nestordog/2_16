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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import ch.algotrader.config.ConfigLocator;

/**
 * Provides XML-persistence methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class XmlUtil {

    /**
     * Writes a {@link Document} to a textFile specified by {@code fileName} and {@code directory}
     * @throws TransformerException
     * @throws IOException
     */
    public static void saveDocumentToFile(Document document, String fileName, String directory) throws TransformerException, IOException {

        boolean saveToFile = ConfigLocator.instance().getConfigParams().getBoolean("misc.saveToFile");
        if (!saveToFile) {
            return;
        }

        File parent = new File("files" + File.separator + directory);
        if (!parent.exists()) {
            FileUtils.forceMkdir(parent);
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
        DOMSource source = new DOMSource(document);

        try (OutputStream out = new FileOutputStream(new File(parent, fileName))) {
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
    }
}
