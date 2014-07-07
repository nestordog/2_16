package ch.algotrader.entity.property;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class PropertyHolderImpl.
 * @see ch.algotrader.entity.property.PropertyHolderImpl
 * @author Hibernate Tools
 */
@Stateless
public class PropertyHolderImplHome {

    private static final Log log = LogFactory.getLog(PropertyHolderImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(PropertyHolderImpl transientInstance) {
        log.debug("persisting PropertyHolderImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(PropertyHolderImpl persistentInstance) {
        log.debug("removing PropertyHolderImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public PropertyHolderImpl merge(PropertyHolderImpl detachedInstance) {
        log.debug("merging PropertyHolderImpl instance");
        try {
            PropertyHolderImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public PropertyHolderImpl findById(int id) {
        log.debug("getting PropertyHolderImpl instance with id: " + id);
        try {
            PropertyHolderImpl instance = entityManager.find(PropertyHolderImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
