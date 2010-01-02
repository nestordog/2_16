package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import com.algoTrader.entity.Rule;
import com.algoTrader.util.CustomDate;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;

public class RuleServiceImpl extends RuleServiceBase {

    protected void handleActivateAll() throws java.lang.Exception {

        Collection col = getRuleDao().findActiveRules();

        for (Iterator it = col.iterator(); it.hasNext();) {
            Rule rule = (Rule)it.next();
            activate(rule);
        }
    }

    protected void handleActivate(String ruleName) throws Exception {

        Rule rule = getRuleDao().findByName(ruleName);
        activate(rule);
    }

    protected void handleActivate(Rule rule) throws java.lang.Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPAdministrator cepAdm = cep.getEPAdministrator();

        EPStatement cepStatement;
        if (rule.isPattern()) {
            cepStatement = cepAdm.createPattern(rule.getDefinition(), rule.getName());
        } else {
            cepStatement = cepAdm.createEPL(rule.getDefinition(), rule.getName());
        }

        if (rule.getSubscriber() != null) {
            Class cl = Class.forName("com.algoTrader.subscriber." + rule.getSubscriber());
            Object obj = cl.newInstance();
            cepStatement.setSubscriber(obj);
        }

        if (rule.getListeners() != null) {
            String[] listeners = rule.getListeners().split("\\s");
            for (int i = 0; i < listeners.length; i++) {
                Class cl = Class.forName("com.algoTrader.listener." + listeners[i]);
                Object obj = cl.newInstance();
                if (obj instanceof StatementAwareUpdateListener) {
                    cepStatement.addListener((StatementAwareUpdateListener)obj);
                } else {
                    cepStatement.addListener((UpdateListener)obj);
                }
            }
        }
    }

    protected void handleDeactivate(String ruleName) throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        EPStatement statement = cep.getEPAdministrator().getStatement(ruleName);
        statement.destroy();
    }

    protected void handleDeactivateAll() throws Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        cep.destroy();
    }

    protected void handleSendEvent(Object object) throws java.lang.Exception {

        EPServiceProvider cep = EsperService.getEPServiceInstance();
        cep.getEPRuntime().sendEvent(object);
    }
}
