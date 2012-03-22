package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IBSecurityRetrieverService;

public class SecurityRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBSecurityRetrieverService service = ServiceLocator.instance().getService("iBSecurityRetrieverService", IBSecurityRetrieverService.class);

        service.init();

        for (String arg : args) {

            int underlyingId = Integer.parseInt(arg);
            service.retrieve(underlyingId);
        }

        ServiceLocator.instance().shutdown();
    }
}
