/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.wiring.common;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import ch.algotrader.config.ConfigParams;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
final class ConfigParamAccessor implements PropertyAccessor {

    ConfigParamAccessor() {
    }

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class<?>[]{ConfigParams.class};
    }

    @Override
    public boolean canRead(final EvaluationContext context, final Object target, final String name) throws AccessException {
        return true;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        ConfigParams configParams = (ConfigParams) target;
        Object value = configParams.getParameter(name, Object.class);
        if (value == null) {
            throw new AccessException("Unknown config parameter '" + name + "'");
        }
        return new TypedValue(value);
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
    }

}