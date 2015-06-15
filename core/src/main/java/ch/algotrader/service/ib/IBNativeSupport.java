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
package ch.algotrader.service.ib;

import ch.algotrader.adapter.ib.IBSessionException;
import ch.algotrader.service.ExternalServiceException;

final class IBNativeSupport {

    public static RuntimeException rethrow(final Throwable cause) {
        if (cause instanceof IBSessionException) {
            int code = ((IBSessionException) cause).getCode();
            String message = cause.getMessage();
            switch (code) {
                case 162:
                    if (message.contains("data request pacing violation")) {
                        return new IBPacingViolationException(message, (IBSessionException) cause);
                    } else {
                        return new ExternalServiceException(message, (IBSessionException) cause);
                    }
                default:
                    return new ExternalServiceException(message, (IBSessionException) cause);
            }
        } if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        } else {
            ExternalServiceException ex = new ExternalServiceException("Failed to retrieve contract details", null);
            if (cause != null) {
                ex.initCause(cause);
            }
            return ex;
        }
    }

}
