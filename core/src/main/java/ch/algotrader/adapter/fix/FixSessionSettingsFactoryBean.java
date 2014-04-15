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
package ch.algotrader.adapter.fix;

import java.io.InputStream;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import quickfix.SessionSettings;

/**
 * Factory bean for FIX {@link quickfix.SessionSettings}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FixSessionSettingsFactoryBean implements FactoryBean<SessionSettings> {

    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public SessionSettings getObject() throws Exception {
        Resource resource = this.resource != null ? this.resource : new ClassPathResource("/fix.cfg");
        InputStream inputStream = resource.getInputStream();
        try {
            return new SessionSettings(inputStream);
        } finally {
            inputStream.close();
        }
    }

    @Override
    public Class<?> getObjectType() {
        return SessionSettings.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
