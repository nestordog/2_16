package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class StockImpl.
 * @see ch.algotrader.entity.security.StockImpl
 * @author Hibernate Tools
 */
@Stateless
public class StockImplHome {

    private static final Log log = LogFactory.getLog(StockImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(StockImpl transientInstance) {
        log.debug("persisting StockImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(StockImpl persistentInstance) {
        log.debug("removing StockImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public StockImpl merge(StockImpl detachedInstance) {
        log.debug("merging StockImpl instance");
        try {
            StockImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public StockImpl findById(int id) {
        log.debug("getting StockImpl instance with id: " + id);
        try {
            StockImpl instance = entityManager.find(StockImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
