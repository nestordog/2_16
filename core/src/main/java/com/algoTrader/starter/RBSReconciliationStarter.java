package com.algoTrader.starter;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.rbs.RBSReconciliationService;

public class RBSReconciliationStarter {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        RBSReconciliationService service = ServiceLocator.instance().getService("rBSReconciliationService", RBSReconciliationService.class);

        service.reconcile(Arrays.asList(args[0]));

        ServiceLocator.instance().shutdown();
    }
}
