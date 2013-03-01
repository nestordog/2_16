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
package com.algoTrader.starter;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IBReconciliationService;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBReconciliationStarter {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        String fileName = args[0];

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBReconciliationService service = ServiceLocator.instance().getService("iBReconciliationService", IBReconciliationService.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File("files" + File.separator + "ib" + File.separator + fileName));

        for (int i = 1; i < args.length; i++) {

            String type = args[i];
            if ("CASH".equals(type)) {
                service.processCashTransactions(document);
            } else if ("POSITIONS".equals(type)) {
                service.reconcilePositions(document);
            } else if ("TRADES".equals(type)) {
                service.reconcileTrades(document);
            } else if ("UNBOOKED_TRADES".equals(type)) {
                service.reconcileUnbookedTrades(document);
            }
        }

        ServiceLocator.instance().shutdown();
    }
}
