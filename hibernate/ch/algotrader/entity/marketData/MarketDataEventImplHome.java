package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class MarketDataEventImpl.
 * @see ch.algotrader.entity.marketData.MarketDataEventImpl
 * @author Hibernate Tools
 */
@Stateless
public class MarketDataEventImplHome {

    private static final Log log = LogFactory.getLog(MarketDataEventImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(MarketDataEventImpl transientInstance) {
        log.debug("persisting MarketDataEventImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(MarketDataEventImpl persistentInstance) {
        log.debug("removing MarketDataEventImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public MarketDataEventImpl merge(MarketDataEventImpl detachedInstance) {
        log.debug("merging MarketDataEventImpl instance");
        try {
            MarketDataEventImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public MarketDataEventImpl findById(int id) {
        log.debug("getting MarketDataEventImpl instance with id: " + id);
        try {
            MarketDataEventImpl instance = entityManager.find(MarketDataEventImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
