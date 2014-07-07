package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class TickImpl.
 * @see ch.algotrader.entity.marketData.TickImpl
 * @author Hibernate Tools
 */
@Stateless
public class TickImplHome {

    private static final Log log = LogFactory.getLog(TickImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(TickImpl transientInstance) {
        log.debug("persisting TickImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(TickImpl persistentInstance) {
        log.debug("removing TickImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public TickImpl merge(TickImpl detachedInstance) {
        log.debug("merging TickImpl instance");
        try {
            TickImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public TickImpl findById(int id) {
        log.debug("getting TickImpl instance with id: " + id);
        try {
            TickImpl instance = entityManager.find(TickImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
