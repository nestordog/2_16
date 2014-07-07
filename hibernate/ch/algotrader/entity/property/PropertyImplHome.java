package ch.algotrader.entity.property;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class PropertyImpl.
 * @see ch.algotrader.entity.property.PropertyImpl
 * @author Hibernate Tools
 */
@Stateless
public class PropertyImplHome {

    private static final Log log = LogFactory.getLog(PropertyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(PropertyImpl transientInstance) {
        log.debug("persisting PropertyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(PropertyImpl persistentInstance) {
        log.debug("removing PropertyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public PropertyImpl merge(PropertyImpl detachedInstance) {
        log.debug("merging PropertyImpl instance");
        try {
            PropertyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public PropertyImpl findById(int id) {
        log.debug("getting PropertyImpl instance with id: " + id);
        try {
            PropertyImpl instance = entityManager.find(PropertyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
