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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountVO;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionVO;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeVO;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyVO;
import ch.algotrader.entity.security.SecurityVO;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyVO;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.rest.index.SecurityIndexer;
import ch.algotrader.service.LookupService;
import ch.algotrader.vo.InternalErrorVO;

@RestController
@RequestMapping(path = "/rest")
public class LookupRestController extends RestControllerBase {

    private final LookupService lookupService;
    private final SecurityIndexer securityIndexer;

    public LookupRestController(
            final LookupService lookupService,
            final SecurityIndexer securityIndexer) {
        this.lookupService = lookupService;
        this.securityIndexer = securityIndexer;
    }

    @CrossOrigin
    @RequestMapping(path = "/security", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SecurityVO> getSecurities() {

        return lookupService.getAllSecurities().stream()
                .map(Security::convertToVO)
                .collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/security-family", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SecurityFamilyVO> getSecurityFamilies() {

        return lookupService.getAllSecurities().stream()
                .map(Security::getSecurityFamily)
                .map(SecurityFamily::convertToVO)
                .collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/security.search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SecurityVO> searchSecurities(
            @RequestParam(value = "query", required = false) String query) throws ParseException {

        if (StringUtils.isEmpty(query)) {
            return Collections.emptyList();
        } else {
            return securityIndexer.search(query);
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/forex", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ForexVO getForex(
        @RequestParam(value = "baseCurrency", required = true) String baseCurrency,
        @RequestParam(value = "transactionCurrency", required = true) String transactionCurrency) throws ParseException {

        Currency _baseCurrency = Currency.valueOf(baseCurrency);
        Currency _transactionCurrency = Currency.valueOf(transactionCurrency);

        return Optional.ofNullable(lookupService.getForex(_baseCurrency, _transactionCurrency)).map(Forex::convertToVO)
            .orElseThrow(() -> new EntityNotFoundException("Forex not found not found base " + baseCurrency + " transaction " + transactionCurrency));
    }

    @CrossOrigin
    @RequestMapping(path = "/security/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SecurityVO getSecurity(@PathVariable long id) {

        return Optional.ofNullable(lookupService.getSecurity(id))
                .map(Security::convertToVO).orElseThrow(() -> new EntityNotFoundException("Security not found: " + id));
    }

    @CrossOrigin
    @RequestMapping(path = "/security-family/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SecurityFamilyVO getSecurityFamily(@PathVariable long id) {

        return Optional.ofNullable(lookupService.getSecurityFamilyBySecurity(id))
                .map(SecurityFamily::convertToVO).orElseThrow(() -> new EntityNotFoundException("Security family not found: " + id));
    }

    @CrossOrigin
    @RequestMapping(path = "/strategy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StrategyVO> getStrategies() {

        return lookupService.getAllStrategies().stream()
                .map(Strategy::convertToVO).collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/account/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountVO getAccountByName(@PathVariable final String name) {

        return Optional.ofNullable(lookupService.getAccountByName(name))
                .map(Account::convertToVO).orElseThrow(() -> new EntityNotFoundException("Account not found: " + name));
    }

    @CrossOrigin
    @RequestMapping(path = "/account", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AccountVO> getAccounts() {

        return lookupService.getAllAccounts().stream()
                .map(Account::convertToVO)
                .collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/exchange", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExchangeVO> getExchanges() {

        return lookupService.getAllExchanges().stream()
                .map(Exchange::convertToVO)
                .collect(Collectors.toList());
    }

    @CrossOrigin
    @RequestMapping(path = "/position", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PositionVO> getPositions(@RequestParam(name = "strategy", required = false) final String strategyName) {

        if (StringUtils.isBlank(strategyName)) {
            return lookupService.getAllPositions().stream()
                    .map(Position::convertToVO)
                    .collect(Collectors.toList());
        } else {
            return lookupService.getPositionsByStrategy(strategyName).stream()
                    .map(Position::convertToVO)
                    .collect(Collectors.toList());
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/position/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PositionVO getPosition(@PathVariable final long id) {

        return Optional.ofNullable(lookupService.getPosition(id))
                .map(Position::convertToVO).orElseThrow(() -> new EntityNotFoundException("Position not found: " + id));
    }

    @CrossOrigin
    @RequestMapping(path = "/transaction/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionVO getTransaction(@PathVariable long id) {

        return Optional.ofNullable(lookupService.getTransaction(id))
                .map(Transaction::convertToVO).orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
    }

    @CrossOrigin
    @RequestMapping(path = "/transaction/daily", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TransactionVO> getTransactions(
            @RequestParam(name = "strategy", required = false) final String strategyName,
            @RequestParam(name = "limit", required = false, defaultValue = "0") int limit) {

        if (StringUtils.isBlank(strategyName)) {
            return lookupService.getDailyTransactions(limit).stream()
                    .map(Transaction::convertToVO)
                    .collect(Collectors.toList());
        } else {
            return lookupService.getDailyTransactionsByStrategy(strategyName, limit).stream()
                    .map(Transaction::convertToVO)
                    .collect(Collectors.toList());
        }
    }

    @ExceptionHandler()
    public InternalErrorVO handleLuceneParseException(final HttpServletResponse response, final ParseException ex) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new InternalErrorVO(ex.getClass(), ex.getMessage());
    }

}
