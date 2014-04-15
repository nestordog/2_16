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
package ch.algotrader.wizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * OSGi-bundle activator
 *
 * @author <a href="mailto:akhikhl@gmail.com">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class Activator extends AbstractUIPlugin {

    private static Activator instance = null;

    public Activator() {
    }

    public static Activator getInstance() {
        return instance;
    }

    public ImageDescriptor getImage(String key, String path) {
        ImageDescriptor descr = this.getImageRegistry().getDescriptor(key);
        if (descr == null) {
            try {
                descr = ImageDescriptor.createFromURL(new URL("platform:/plugin/" + this.getBundle().getSymbolicName() + "/" + path));
                this.getImageRegistry().put(key, descr);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return descr;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        instance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        instance = null;
        super.stop(context);
    }
}
