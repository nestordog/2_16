package com.algoTrader.service;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Rule;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPAdministrator;
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

    protected void handleActivate(String ruleName) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);
        activate(rule, rule.getPrioritisedDefinition());
    }

    protected void handleActivate(String ruleName, String[] parameters) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);
        if (rule == null) throw new Exception("no rule for the name" + ruleName + " was found");

        MessageFormat format = new MessageFormat(rule.getPrioritisedDefinition());
        String definition = format.format(parameters);

        activate(rule, definition);
    }

    protected void handleActivate(Rule rule) throws java.lang.Exception {

        activate(rule, rule.getPrioritisedDefinition());
    }

    private void activate(Rule rule, String definition) throws java.lang.Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPAdministrator cepAdm = cep.getEPAdministrator();

        // deactivate the statement if it already exists
        EPStatement oldStatement = cep.getEPAdministrator().getStatement(rule.getName());
        if (oldStatement != null) {
            oldStatement.destroy();
        }

        // create the new statement
        EPStatement newStatement;
        if (rule.isPattern()) {
            newStatement = cepAdm.createPattern(definition, rule.getName());
        } else {
            newStatement = cepAdm.createEPL(definition, rule.getName());
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

    protected void handleDeactivate(String ruleName) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPStatement statement = cep.getEPAdministrator().getStatement(ruleName);

        if (statement != null) {
            statement.destroy();
            logger.debug("deactivated rule " + ruleName);
        } else {
            logger.debug("rule to be deactivated does not exist: " + ruleName);
        }
    }

    protected void handleDeactivateAll() throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        cep.destroy();

        logger.debug("activated all rules");
    }

    protected boolean handleIsActive(String ruleName) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPStatement statement = cep.getEPAdministrator().getStatement(ruleName);

        if (statement != null && statement.isStarted()) {
            return true;
        } else {
            return false;
        }
    }
}
