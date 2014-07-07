package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class ExchangeImpl.
 * @see ch.algotrader.entity.security.ExchangeImpl
 * @author Hibernate Tools
 */
@Stateless
public class ExchangeImplHome {

    private static final Log log = LogFactory.getLog(ExchangeImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(ExchangeImpl transientInstance) {
        log.debug("persisting ExchangeImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(ExchangeImpl persistentInstance) {
        log.debug("removing ExchangeImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public ExchangeImpl merge(ExchangeImpl detachedInstance) {
        log.debug("merging ExchangeImpl instance");
        try {
            ExchangeImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public ExchangeImpl findById(int id) {
        log.debug("getting ExchangeImpl instance with id: " + id);
        try {
            ExchangeImpl instance = entityManager.find(ExchangeImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
