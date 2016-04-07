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

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.service.ServiceException;
import ch.algotrader.vo.InternalErrorVO;

@RestController
public class RestControllerBase {

    private final Logger LOGGER = LogManager.getLogger(RestControllerBase.class);

    @ExceptionHandler()
    public InternalErrorVO handleServiceException(final HttpServletResponse response, final ServiceException ex) {

        LOGGER.error("Service exception: " + ex.getMessage());

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new InternalErrorVO(ex.getClass(), ex.getMessage());
    }

    @ExceptionHandler()
    public InternalErrorVO handleEntityNotFound(final HttpServletResponse response, final EntityNotFoundException ex) {

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new InternalErrorVO(ex.getClass(), ex.getMessage());
    }

    @ExceptionHandler()
    public InternalErrorVO handleIllegalArgumentException(final HttpServletResponse response, final IllegalArgumentException ex) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new InternalErrorVO(ex.getClass(), ex.getMessage());
    }

    @ExceptionHandler()
    public InternalErrorVO handleTypeMismatchException(final HttpServletResponse response, final TypeMismatchException ex) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new InternalErrorVO(ex.getClass(), ex.getMessage());
    }

    @ExceptionHandler()
    public InternalErrorVO handleUnrecoverableException(final HttpServletResponse response, final RuntimeException ex) {

        LOGGER.error(ex.getMessage(), ex);

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new InternalErrorVO(ex.getClass(), ex.getMessage());
    }

}
