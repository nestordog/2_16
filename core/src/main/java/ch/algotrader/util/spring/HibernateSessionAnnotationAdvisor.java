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

package ch.algotrader.util.spring;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.orm.hibernate3.HibernateInterceptor;

/**
 * Advisor that creates Hibernate Sessions the {@link HibernateSession}
 * annotation. This annotation can be used at type level in
 * implementation classes as well as in service interfaces.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class HibernateSessionAnnotationAdvisor extends AbstractPointcutAdvisor {

    private HibernateInterceptor hibernateInterceptor;
    private Pointcut pointcut;

    public HibernateSessionAnnotationAdvisor() {
        this.pointcut = new AnnotationMatchingPointcut(HibernateSession.class, true);
    }

    public void setHibernateInterceptor(HibernateInterceptor hibernateInterceptor) {
        this.hibernateInterceptor = hibernateInterceptor;
    }

    @Override
    public Advice getAdvice() {
        return this.hibernateInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
