package com.algoTrader.starter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ui.UIReconciliationService;

public class UIReconciliationStarter {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        UIReconciliationService service = ServiceLocator.instance().getService("uIReconciliationService", UIReconciliationService.class);

        File file = new File(args[0]);
        InputStream bis = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(bis, bos);

        service.reconcile(file.getName(), bos.toByteArray());

        ServiceLocator.instance().shutdown();
    }
}
