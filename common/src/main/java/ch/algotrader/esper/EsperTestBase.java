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
package ch.algotrader.esper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.deploy.Module;
import com.espertech.esper.client.deploy.ModuleItem;
import com.espertech.esper.client.deploy.ParseException;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.core.deploy.EPLModuleUtil;

public class EsperTestBase {

    protected static Module load(final Reader in, final String name) throws IOException, ParseException {
        StringWriter buffer = new StringWriter();
        IOUtils.copy(in, buffer);
        return EPLModuleUtil.parseInternal(buffer.toString(), name);
    }

    protected static Module load(final URL resource, final String name) throws IOException, ParseException {
        try (Reader reader = new InputStreamReader(resource.openStream(), Charsets.ISO_8859_1)) {
            return load(reader, name);
        }
    }

    protected static void ensureCompilable(
            final EPServiceProvider epServiceProvider,
            final URL resource) throws IOException, ParseException {
        EPAdministrator epAdministrator = epServiceProvider.getEPAdministrator();
        Module module = load(resource, "test");
        for (ModuleItem moduleItem : module.getItems()) {
            String expression = moduleItem.getExpression();
            EPStatementObjectModel compiledStatement = epAdministrator.compileEPL(expression);
            epAdministrator.create(compiledStatement);
        }
    }

    protected static void deployModule(
            final EPServiceProvider epServiceProvider,
            final URL resource,
            final String... includedStatements) throws IOException, ParseException {

        Set<String> statementNames = new LinkedHashSet<>();
        for (String includedStatement: includedStatements) {
            statementNames.add(includedStatement);
        }
        Module module = load(resource, "unit-test");
        EPAdministrator epAdministrator = epServiceProvider.getEPAdministrator();
        for (ModuleItem moduleItem : module.getItems()) {
            String expression = moduleItem.getExpression();
            EPStatementObjectModel compiledStatement = epAdministrator.compileEPL(expression);
            boolean included = true;
            if (!statementNames.isEmpty()) {
                for (AnnotationPart annotationPart: compiledStatement.getAnnotations()) {
                    if (annotationPart.getName().equals("Name")) {
                        for (AnnotationAttribute attribute: annotationPart.getAttributes()) {
                            if (attribute.getName().equals("value")) {
                                Object name = attribute.getValue();
                                included = statementNames.contains(name);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            if (included) {
                epAdministrator.create(compiledStatement);
            }
        }
    }

    protected static EPStatement deployPreparedStatement(
            final EPServiceProvider epServiceProvider,
            final URL resource,
            final String name,
            final Object... params) throws IOException, ParseException {

        Module module = load(resource, "unit-test");
        EPAdministrator epAdministrator = epServiceProvider.getEPAdministrator();
        for (ModuleItem moduleItem : module.getItems()) {
            String expression = moduleItem.getExpression();
            EPStatementObjectModel compiledStatement = epAdministrator.compileEPL(expression.replace('?', '1'));
            for (AnnotationPart annotationPart: compiledStatement.getAnnotations()) {
                if (annotationPart.getName().equals("Name")) {
                    for (AnnotationAttribute attribute: annotationPart.getAttributes()) {
                        if (attribute.getName().equals("value")) {
                            if (name.equals(attribute.getValue())) {
                                EPPreparedStatement preparedStatement = epAdministrator.prepareEPL(expression);
                                for (int i = 0; i < params.length; i++) {
                                    preparedStatement.setObject(i + 1, params[i]);
                                }
                                return epAdministrator.create(preparedStatement, name);
                            }
                        }
                    }
                    break;
                }
            }
        }
        throw new IllegalStateException("Statement '" + name + "' not found");
    }

}

