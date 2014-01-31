package ch.algotrader.util.spring;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
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
public class HibernateSessionAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        ConfigurableListableBeanFactory configurableListableBeanFactory = (ConfigurableListableBeanFactory) beanFactory;
        AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) configurableListableBeanFactory.getBeanDefinition("hibernateSessionAnnotationAdvisor");
        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    }
}
