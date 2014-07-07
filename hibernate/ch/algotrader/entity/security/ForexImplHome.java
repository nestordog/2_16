package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class ForexImpl.
 * @see ch.algotrader.entity.security.ForexImpl
 * @author Hibernate Tools
 */
@Stateless
public class ForexImplHome {

    private static final Log log = LogFactory.getLog(ForexImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(ForexImpl transientInstance) {
        log.debug("persisting ForexImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(ForexImpl persistentInstance) {
        log.debug("removing ForexImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public ForexImpl merge(ForexImpl detachedInstance) {
        log.debug("merging ForexImpl instance");
        try {
            ForexImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public ForexImpl findById(int id) {
        log.debug("getting ForexImpl instance with id: " + id);
        try {
            ForexImpl instance = entityManager.find(ForexImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
