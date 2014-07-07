package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class OrderPreferenceImpl.
 * @see ch.algotrader.entity.strategy.OrderPreferenceImpl
 * @author Hibernate Tools
 */
@Stateless
public class OrderPreferenceImplHome {

    private static final Log log = LogFactory.getLog(OrderPreferenceImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(OrderPreferenceImpl transientInstance) {
        log.debug("persisting OrderPreferenceImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(OrderPreferenceImpl persistentInstance) {
        log.debug("removing OrderPreferenceImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public OrderPreferenceImpl merge(OrderPreferenceImpl detachedInstance) {
        log.debug("merging OrderPreferenceImpl instance");
        try {
            OrderPreferenceImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public OrderPreferenceImpl findById(int id) {
        log.debug("getting OrderPreferenceImpl instance with id: " + id);
        try {
            OrderPreferenceImpl instance = entityManager.find(OrderPreferenceImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
