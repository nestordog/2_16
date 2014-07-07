package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class TradingHoursImpl.
 * @see ch.algotrader.entity.security.TradingHoursImpl
 * @author Hibernate Tools
 */
@Stateless
public class TradingHoursImplHome {

    private static final Log log = LogFactory.getLog(TradingHoursImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(TradingHoursImpl transientInstance) {
        log.debug("persisting TradingHoursImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(TradingHoursImpl persistentInstance) {
        log.debug("removing TradingHoursImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public TradingHoursImpl merge(TradingHoursImpl detachedInstance) {
        log.debug("merging TradingHoursImpl instance");
        try {
            TradingHoursImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public TradingHoursImpl findById(int id) {
        log.debug("getting TradingHoursImpl instance with id: " + id);
        try {
            TradingHoursImpl instance = entityManager.find(TradingHoursImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
