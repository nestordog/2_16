package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class OptionImpl.
 * @see ch.algotrader.entity.security.OptionImpl
 * @author Hibernate Tools
 */
@Stateless
public class OptionImplHome {

    private static final Log log = LogFactory.getLog(OptionImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(OptionImpl transientInstance) {
        log.debug("persisting OptionImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(OptionImpl persistentInstance) {
        log.debug("removing OptionImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public OptionImpl merge(OptionImpl detachedInstance) {
        log.debug("merging OptionImpl instance");
        try {
            OptionImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public OptionImpl findById(int id) {
        log.debug("getting OptionImpl instance with id: " + id);
        try {
            OptionImpl instance = entityManager.find(OptionImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
