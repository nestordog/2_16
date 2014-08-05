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
package ch.algotrader.configeditor;

import java.util.ArrayList;
import java.util.List;

import ch.algotrader.configeditor.editingsupport.PropertyDefExtensionPoint;

/**
 * Represents structured value (value + comments).
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class ValueStruct {

    public Object value;
    public List<String> comments;
    public String inlineComment;

    public ValueStruct() {
        comments = new ArrayList<String>();
    }

    public ValueStruct(Object pValue) {
        comments = new ArrayList<String>();
        value = pValue;
    }

    public String getSaveReadyValue() {
        String propertyId = new PropertyModel(this).getPropertyId();
        return PropertyDefExtensionPoint.serialize(propertyId, value);
    }
}
