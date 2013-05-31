// AlgoTrader: 35 - 36 for Lists use our ListDatimport java.awt.Color;
/*
 * @(#)XArrayDataViewer.java    1.12 07/05/30
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.tools.jconsole.inspector;

import java.awt.Component;

import ch.algorader.client.DataViewer;

class XArrayDataViewer {

    private XArrayDataViewer() {}

    public static boolean isViewableValue(Object value) {
        return Utils.canBeRenderedAsArray(value);
    }

    public static Component loadArray(Object value) {
        if (isViewableValue(value)) {
            return new DataViewer(value);
        }
        return null;
    }
}
