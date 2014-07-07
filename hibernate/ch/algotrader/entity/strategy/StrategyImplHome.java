package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class StrategyImpl.
 * @see ch.algotrader.entity.strategy.StrategyImpl
 * @author Hibernate Tools
 */
@Stateless
public class StrategyImplHome {

    private static final Log log = LogFactory.getLog(StrategyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(StrategyImpl transientInstance) {
        log.debug("persisting StrategyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(StrategyImpl persistentInstance) {
        log.debug("removing StrategyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public StrategyImpl merge(StrategyImpl detachedInstance) {
        log.debug("merging StrategyImpl instance");
        try {
            StrategyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public StrategyImpl findById(int id) {
        log.debug("getting StrategyImpl instance with id: " + id);
        try {
            StrategyImpl instance = entityManager.find(StrategyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
