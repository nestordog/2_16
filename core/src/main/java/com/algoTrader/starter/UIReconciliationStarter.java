package com.algoTrader.starter;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ui.UIReconciliationService;

public class UIReconciliationStarter {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        UIReconciliationService service = ServiceLocator.instance().getService("uIReconciliationService", UIReconciliationService.class);

        service.reconcile(Arrays.asList(args[0]));

        ServiceLocator.instance().shutdown();
    }
}
