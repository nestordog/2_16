package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class SecurityImpl.
 * @see ch.algotrader.entity.security.SecurityImpl
 * @author Hibernate Tools
 */
@Stateless
public class SecurityImplHome {

    private static final Log log = LogFactory.getLog(SecurityImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(SecurityImpl transientInstance) {
        log.debug("persisting SecurityImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(SecurityImpl persistentInstance) {
        log.debug("removing SecurityImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public SecurityImpl merge(SecurityImpl detachedInstance) {
        log.debug("merging SecurityImpl instance");
        try {
            SecurityImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public SecurityImpl findById(int id) {
        log.debug("getting SecurityImpl instance with id: " + id);
        try {
            SecurityImpl instance = entityManager.find(SecurityImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
