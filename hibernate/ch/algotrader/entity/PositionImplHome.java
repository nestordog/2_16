package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class PositionImpl.
 * @see ch.algotrader.entity.PositionImpl
 * @author Hibernate Tools
 */
@Stateless
public class PositionImplHome {

    private static final Log log = LogFactory.getLog(PositionImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(PositionImpl transientInstance) {
        log.debug("persisting PositionImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(PositionImpl persistentInstance) {
        log.debug("removing PositionImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public PositionImpl merge(PositionImpl detachedInstance) {
        log.debug("merging PositionImpl instance");
        try {
            PositionImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public PositionImpl findById(int id) {
        log.debug("getting PositionImpl instance with id: " + id);
        try {
            PositionImpl instance = entityManager.find(PositionImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
