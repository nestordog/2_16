package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class DefaultOrderPreferenceImpl.
 * @see ch.algotrader.entity.strategy.DefaultOrderPreferenceImpl
 * @author Hibernate Tools
 */
@Stateless
public class DefaultOrderPreferenceImplHome {

    private static final Log log = LogFactory.getLog(DefaultOrderPreferenceImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(DefaultOrderPreferenceImpl transientInstance) {
        log.debug("persisting DefaultOrderPreferenceImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(DefaultOrderPreferenceImpl persistentInstance) {
        log.debug("removing DefaultOrderPreferenceImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public DefaultOrderPreferenceImpl merge(DefaultOrderPreferenceImpl detachedInstance) {
        log.debug("merging DefaultOrderPreferenceImpl instance");
        try {
            DefaultOrderPreferenceImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public DefaultOrderPreferenceImpl findById(int id) {
        log.debug("getting DefaultOrderPreferenceImpl instance with id: " + id);
        try {
            DefaultOrderPreferenceImpl instance = entityManager.find(DefaultOrderPreferenceImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
