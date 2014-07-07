package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class CombinationImpl.
 * @see ch.algotrader.entity.security.CombinationImpl
 * @author Hibernate Tools
 */
@Stateless
public class CombinationImplHome {

    private static final Log log = LogFactory.getLog(CombinationImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(CombinationImpl transientInstance) {
        log.debug("persisting CombinationImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(CombinationImpl persistentInstance) {
        log.debug("removing CombinationImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public CombinationImpl merge(CombinationImpl detachedInstance) {
        log.debug("merging CombinationImpl instance");
        try {
            CombinationImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public CombinationImpl findById(int id) {
        log.debug("getting CombinationImpl instance with id: " + id);
        try {
            CombinationImpl instance = entityManager.find(CombinationImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
