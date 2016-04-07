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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.TransactionVO;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.TransactionService;

@RestController
@RequestMapping(path = "/rest")
public class TransactionRestController extends RestControllerBase {

    private final TransactionService transactionService;
    private final LookupService lookupService;

    public TransactionRestController(final TransactionService transactionService, final LookupService lookupService) {
        this.transactionService = transactionService;
        this.lookupService = lookupService;
    }

    @CrossOrigin
    @RequestMapping(path = "/transaction", method = RequestMethod.POST)
    public void createTransaction(@RequestBody TransactionVO transaction) {

        Strategy strategy = this.lookupService.getStrategy(transaction.getStrategyId());
        if (strategy == null) {
            throw new EntityNotFoundException("Strategy not found: " + transaction.getStrategyId());
        }

        String accountName = transaction.getAccountId() != 0 ? getAccountName(transaction.getAccountId()) : null;

        this.transactionService.createTransaction(transaction.getSecurityId(), strategy.getName(), transaction.getExtId(), transaction.getDateTime(),
                transaction.getQuantity(), transaction.getPrice(), transaction.getExecutionCommission(), transaction.getClearingCommission(), transaction.getFee(), transaction.getCurrency(),
                transaction.getType(), accountName, transaction.getDescription());
    }

    @CrossOrigin
    @RequestMapping(path = "/transaction/reset-cache-balances", method = RequestMethod.POST)
    public void resetCashBalances() {

        this.transactionService.resetCashBalances();
    }


    private String getAccountName(long accountId){
        Account account = this.lookupService.getAccount(accountId);
        if (account == null) {
            throw new EntityNotFoundException("Account not found: " + accountId);
        }

        return account.getName();
    }

}
