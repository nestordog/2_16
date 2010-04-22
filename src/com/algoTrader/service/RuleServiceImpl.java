package com.algoTrader.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;

public class RuleServiceImpl extends RuleServiceBase {

    private static Logger logger = MyLogger.getLogger(RuleServiceImpl.class.getName());

    @SuppressWarnings("unchecked")
    protected void handleActivateAll() throws java.lang.Exception {

        List<Rule> list = getRuleDao().findActivatableRules();
        for (Rule rule : list) {
            activate(rule);
        }
    }

    protected void handleActivate(String ruleName) throws Exception {

        activate(RuleName.fromString(ruleName));
    }

    protected void handleActivate(RuleName ruleName) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);
        activate(rule);
    }

    protected void handleActivate(RuleName ruleName, Security target) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);

        if (!rule.isPrepared()) throw new RuleServiceException("target is allowed only on prepared rules");

        rule.setTarget(target);
        getRuleDao().update(rule);
        activate(rule);
    }

    @SuppressWarnings("unchecked")
    protected void handleActivate(Rule rule) throws java.lang.Exception {

        String definition = rule.getPrioritisedDefinition();
        String name = rule.getName().getValue();

        EPAdministrator cepAdm = EsperService.getEPServiceInstance().getEPAdministrator();

        // do nothing if the statement already exists
        EPStatement oldStatement = cepAdm.getStatement(name);
        if (oldStatement != null && oldStatement.isStarted()) {
            return;
        }

        // create the new statement
        EPStatement newStatement;
        if (rule.isPrepared()) {
            EPPreparedStatement prepared = cepAdm.prepareEPL(definition);
            prepared.setObject(1, rule.getTarget().getId());
            newStatement = cepAdm.create(prepared, name);
        } else {
            if (rule.isPattern()) {
                newStatement = cepAdm.createPattern(definition, name);
            } else {
                newStatement = cepAdm.createEPL(definition, name);
            }
        }

        // add the subscribers
        if (rule.getSubscriber() != null) {
            Class cl = Class.forName("com.algoTrader.subscriber." + rule.getSubscriber().trim());
            Object obj = cl.newInstance();
            newStatement.setSubscriber(obj);
        }

        // add the listeners
        if (rule.getListeners() != null) {
            String[] listeners = rule.getListeners().split("\\s");
            for (String listener : listeners) {
                Class cl = Class.forName("com.algoTrader.listener." + listener);
                Object obj = cl.newInstance();
                if (obj instanceof StatementAwareUpdateListener) {
                    newStatement.addListener((StatementAwareUpdateListener)obj);
                } else {
                    newStatement.addListener((UpdateListener)obj);
                }
            }
        }

        logger.debug("activated rule " + rule.getName() + (rule.isPrepared() ? " on " + rule.getTarget().getSymbol() : ""));
    }

    protected void handleDeactivate(String ruleName) throws Exception {

        deactivate(RuleName.fromString(ruleName));
    }

    protected void handleDeactivate(RuleName ruleName) throws Exception {

        // update the rule entity
        Rule rule = getRuleDao().findByName(ruleName);
        if (rule != null && rule.isPrepared()) {
            rule.setTarget(null);
            getRuleDao().update(rule);
        }

        // destroy the statement
        EPStatement statement = EsperService.getStatement(ruleName);

        if (statement != null) {
            statement.destroy();
            logger.debug("deactivated rule " + ruleName);
        }
    }

    protected void handleDeactivate(RuleName ruleName, Security target) throws Exception {

        // only deactivte if the rule is active, is prepared and has he specified target
        if (isActive(ruleName)) {
            Rule rule = getRuleDao().findByName(ruleName);
            if (rule.isPrepared() && rule.getTarget().equals(target)) {
                deactivate(ruleName);
            }
        }
    }

    protected boolean handleIsActive(RuleName ruleName) throws Exception {

        EPStatement statement = EsperService.getStatement(ruleName);

        if (statement != null && statement.isStarted()) {
            return true;
        } else {
            return false;
        }
    }

    protected void handleSetInternalClock() {

        EsperService.setInternalClock();
        EsperService.enableJmx();
    }
}
