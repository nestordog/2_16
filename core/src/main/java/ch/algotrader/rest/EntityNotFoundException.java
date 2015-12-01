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
package ch.algotrader.rest;

/**
 * Signals failure to locate an entity by its identifier.
 */
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2887478958832423770L;

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Exception ex) {
        super(ex);
    }

    public EntityNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

}
