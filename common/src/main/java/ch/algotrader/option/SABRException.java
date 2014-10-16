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
package ch.algotrader.option;

/**
 * @author <a href="mailto:eburgener@algotrader.ch">Emanuel Burgener</a>
 *
 * @version $Revision$ $Date$
 */
public class SABRException extends Exception {

    private static final long serialVersionUID = 8728454890890104577L;

    public SABRException(String message, Exception ex) {
        super(message, ex);
    }

}
