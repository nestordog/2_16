package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class FutureImpl.
 * @see ch.algotrader.entity.security.FutureImpl
 * @author Hibernate Tools
 */
@Stateless
public class FutureImplHome {

    private static final Log log = LogFactory.getLog(FutureImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(FutureImpl transientInstance) {
        log.debug("persisting FutureImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(FutureImpl persistentInstance) {
        log.debug("removing FutureImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public FutureImpl merge(FutureImpl detachedInstance) {
        log.debug("merging FutureImpl instance");
        try {
            FutureImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public FutureImpl findById(int id) {
        log.debug("getting FutureImpl instance with id: " + id);
        try {
            FutureImpl instance = entityManager.find(FutureImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
