package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class ComponentImpl.
 * @see ch.algotrader.entity.security.ComponentImpl
 * @author Hibernate Tools
 */
@Stateless
public class ComponentImplHome {

    private static final Log log = LogFactory.getLog(ComponentImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(ComponentImpl transientInstance) {
        log.debug("persisting ComponentImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(ComponentImpl persistentInstance) {
        log.debug("removing ComponentImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public ComponentImpl merge(ComponentImpl detachedInstance) {
        log.debug("merging ComponentImpl instance");
        try {
            ComponentImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public ComponentImpl findById(int id) {
        log.debug("getting ComponentImpl instance with id: " + id);
        try {
            ComponentImpl instance = entityManager.find(ComponentImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
