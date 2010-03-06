package com.algoTrader.service;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPPreparedStatement;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;

public class RuleServiceImpl extends RuleServiceBase {

    private static Logger logger = MyLogger.getLogger(RuleServiceImpl.class.getName());

    protected void handleActivateAll() throws java.lang.Exception {

        Collection col = getRuleDao().findActiveRules();

        for (Iterator it = col.iterator(); it.hasNext();) {
            Rule rule = (Rule)it.next();
            activate(rule);
        }
    }

    protected void handleActivate(RuleName ruleName) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);
        activate(rule);
    }

    protected void handleActivate(RuleName ruleName, Security target) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);

        if (!rule.isPrepared()) throw new RuleServiceException("target is allowed only on prepared rules");
        rule.setTarget(target);
        activate(rule);
    }

    protected void handleActivate(Rule rule) throws java.lang.Exception {

        String definition = rule.getPrioritisedDefinition();
        String name = rule.getName().getValue();

        //update the entity
        rule.setActive(true);
        getRuleDao().update(rule);

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPAdministrator cepAdm = cep.getEPAdministrator();

        // deactivate the statement if it already exists
        EPStatement oldStatement = cep.getEPAdministrator().getStatement(name);
        if (oldStatement != null) {
            oldStatement.destroy();
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
            for (int i = 0; i < listeners.length; i++) {
                Class cl = Class.forName("com.algoTrader.listener." + listeners[i]);
                Object obj = cl.newInstance();
                if (obj instanceof StatementAwareUpdateListener) {
                    newStatement.addListener((StatementAwareUpdateListener)obj);
                } else {
                    newStatement.addListener((UpdateListener)obj);
                }
            }
        }

        logger.debug("activated rule " + rule.getName());
    }

    protected void handleDeactivate(RuleName ruleName) throws Exception {

        // update the rule entity
        Rule rule = getRuleDao().findByName(ruleName);
        rule.setTarget(null);
        rule.setActive(false);
        getRuleDao().update(rule);

        // destroy the statement
        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPStatement statement = cep.getEPAdministrator().getStatement(ruleName.getValue());

        statement.destroy();
        logger.debug("deactivated rule " + ruleName);
    }

    protected boolean handleIsActive(RuleName ruleName) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPStatement statement = cep.getEPAdministrator().getStatement(ruleName.getValue());

        if (statement != null && statement.isStarted()) {
            return true;
        } else {
            return false;
        }
    }
}
