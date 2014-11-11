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
package ch.algotrader.service.bb;

import java.text.ParseException;

import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BBReferenceDataServiceException extends ServiceException {

    private static final long serialVersionUID = -2524194330104480381L;

    public BBReferenceDataServiceException(String message) {
        super(message);
    }

    public BBReferenceDataServiceException(ParseException ex) {
        super(ex);
    }

    public BBReferenceDataServiceException(Exception ex) {
        super(ex);
    }

    public BBReferenceDataServiceException(String message, Exception ex) {
        super(message, ex);
    }

}
