package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class GenericFutureImpl.
 * @see ch.algotrader.entity.security.GenericFutureImpl
 * @author Hibernate Tools
 */
@Stateless
public class GenericFutureImplHome {

    private static final Log log = LogFactory.getLog(GenericFutureImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(GenericFutureImpl transientInstance) {
        log.debug("persisting GenericFutureImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(GenericFutureImpl persistentInstance) {
        log.debug("removing GenericFutureImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public GenericFutureImpl merge(GenericFutureImpl detachedInstance) {
        log.debug("merging GenericFutureImpl instance");
        try {
            GenericFutureImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public GenericFutureImpl findById(int id) {
        log.debug("getting GenericFutureImpl instance with id: " + id);
        try {
            GenericFutureImpl instance = entityManager.find(GenericFutureImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
