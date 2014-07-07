package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class IntrestRateImpl.
 * @see ch.algotrader.entity.security.IntrestRateImpl
 * @author Hibernate Tools
 */
@Stateless
public class IntrestRateImplHome {

    private static final Log log = LogFactory.getLog(IntrestRateImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(IntrestRateImpl transientInstance) {
        log.debug("persisting IntrestRateImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(IntrestRateImpl persistentInstance) {
        log.debug("removing IntrestRateImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public IntrestRateImpl merge(IntrestRateImpl detachedInstance) {
        log.debug("merging IntrestRateImpl instance");
        try {
            IntrestRateImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public IntrestRateImpl findById(int id) {
        log.debug("getting IntrestRateImpl instance with id: " + id);
        try {
            IntrestRateImpl instance = entityManager.find(IntrestRateImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
