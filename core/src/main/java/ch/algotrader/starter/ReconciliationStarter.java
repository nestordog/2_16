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
package ch.algotrader.starter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import ch.algotrader.ServiceLocator;
import ch.algotrader.service.ReconciliationService;

/**
 * Starter Class to infoke the reconciliation process based on a file(s)
 * <p>
 * Usage: {@code ReconciliationStarter serviceName fileName(s)}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ReconciliationStarter {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        ReconciliationService service = ServiceLocator.instance().getService(args[0], ReconciliationService.class);

        for (int i = 1; i < args.length; i++) {

            File file = new File(args[i]);
            InputStream bis = new BufferedInputStream(new FileInputStream(file));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(bis, bos);

            service.reconcile(file.getName(), bos.toByteArray());
        }

        ServiceLocator.instance().shutdown();
    }
}
