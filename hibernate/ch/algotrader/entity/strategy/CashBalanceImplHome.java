package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class CashBalanceImpl.
 * @see ch.algotrader.entity.strategy.CashBalanceImpl
 * @author Hibernate Tools
 */
@Stateless
public class CashBalanceImplHome {

    private static final Log log = LogFactory.getLog(CashBalanceImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(CashBalanceImpl transientInstance) {
        log.debug("persisting CashBalanceImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(CashBalanceImpl persistentInstance) {
        log.debug("removing CashBalanceImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public CashBalanceImpl merge(CashBalanceImpl detachedInstance) {
        log.debug("merging CashBalanceImpl instance");
        try {
            CashBalanceImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public CashBalanceImpl findById(int id) {
        log.debug("getting CashBalanceImpl instance with id: " + id);
        try {
            CashBalanceImpl instance = entityManager.find(CashBalanceImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
