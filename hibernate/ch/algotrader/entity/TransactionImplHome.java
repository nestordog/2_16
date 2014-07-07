package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class TransactionImpl.
 * @see ch.algotrader.entity.TransactionImpl
 * @author Hibernate Tools
 */
@Stateless
public class TransactionImplHome {

    private static final Log log = LogFactory.getLog(TransactionImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(TransactionImpl transientInstance) {
        log.debug("persisting TransactionImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(TransactionImpl persistentInstance) {
        log.debug("removing TransactionImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public TransactionImpl merge(TransactionImpl detachedInstance) {
        log.debug("merging TransactionImpl instance");
        try {
            TransactionImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public TransactionImpl findById(int id) {
        log.debug("getting TransactionImpl instance with id: " + id);
        try {
            TransactionImpl instance = entityManager.find(TransactionImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
