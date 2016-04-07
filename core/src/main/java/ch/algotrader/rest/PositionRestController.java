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

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.service.PositionService;

@RestController
@RequestMapping(path = "/rest")
public class PositionRestController extends RestControllerBase {

    private final PositionService positionService;

    public PositionRestController(final PositionService positionService) {
        this.positionService = positionService;
    }

    @CrossOrigin
    @RequestMapping(path = "/position/{id}", method = RequestMethod.DELETE)
    public void closePosition(@PathVariable final long id) {

        this.positionService.closePosition(id, false);
    }

    @CrossOrigin
    @RequestMapping(path = "/position/reduce/{id}", method = RequestMethod.POST)
    public void reducePosition(@PathVariable final long id, @RequestBody final long qty) {

        this.positionService.reducePosition(id, qty);
    }

    @CrossOrigin
    @RequestMapping(path = "/position/reset-positions", method = RequestMethod.POST)
    public void resetPositions() {

        this.positionService.resetPositions();
    }

}
