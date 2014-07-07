package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class BarImpl.
 * @see ch.algotrader.entity.marketData.BarImpl
 * @author Hibernate Tools
 */
@Stateless
public class BarImplHome {

    private static final Log log = LogFactory.getLog(BarImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(BarImpl transientInstance) {
        log.debug("persisting BarImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(BarImpl persistentInstance) {
        log.debug("removing BarImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public BarImpl merge(BarImpl detachedInstance) {
        log.debug("merging BarImpl instance");
        try {
            BarImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public BarImpl findById(int id) {
        log.debug("getting BarImpl instance with id: " + id);
        try {
            BarImpl instance = entityManager.find(BarImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
