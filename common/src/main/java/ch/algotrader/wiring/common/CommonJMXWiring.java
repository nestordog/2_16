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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.MBeanServer;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.support.MBeanServerFactoryBean;

/**
 * Common framework configuration.
 */
@Profile(value = {"live"})
@Configuration
public class CommonJMXWiring {

    @Bean(name = "customEditorConfigurer")
    public static CustomEditorConfigurer createCustomEditorConfigurer() {

        CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
        PropertyEditorRegistrar registrar = registry -> registry.registerCustomEditor(
                Date.class, new CustomDateEditor(new SimpleDateFormat("kk:mm"), false));
        customEditorConfigurer.setPropertyEditorRegistrars(new PropertyEditorRegistrar[] {registrar});
        return customEditorConfigurer;
    }

    @Bean(name = "annotationMBeanExporter")
    public static AnnotationMBeanExporter createAnnotationMBeanExporter() {

        return new AnnotationMBeanExporter();
    }

    @Bean(name = "mbeanServer")
    public MBeanServer createMBeanServer() {

        MBeanServerFactoryBean mBeanServerFactoryBean = new MBeanServerFactoryBean();
        mBeanServerFactoryBean.setLocateExistingServerIfPossible(true);

        return mBeanServerFactoryBean.getObject();
    }

}

