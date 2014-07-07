package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class AllocationImpl.
 * @see ch.algotrader.entity.strategy.AllocationImpl
 * @author Hibernate Tools
 */
@Stateless
public class AllocationImplHome {

    private static final Log log = LogFactory.getLog(AllocationImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(AllocationImpl transientInstance) {
        log.debug("persisting AllocationImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(AllocationImpl persistentInstance) {
        log.debug("removing AllocationImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public AllocationImpl merge(AllocationImpl detachedInstance) {
        log.debug("merging AllocationImpl instance");
        try {
            AllocationImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public AllocationImpl findById(int id) {
        log.debug("getting AllocationImpl instance with id: " + id);
        try {
            AllocationImpl instance = entityManager.find(AllocationImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
