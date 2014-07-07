package ch.algotrader.entity;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class AccountImpl.
 * @see ch.algotrader.entity.AccountImpl
 * @author Hibernate Tools
 */
@Stateless
public class AccountImplHome {

    private static final Log log = LogFactory.getLog(AccountImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(AccountImpl transientInstance) {
        log.debug("persisting AccountImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(AccountImpl persistentInstance) {
        log.debug("removing AccountImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public AccountImpl merge(AccountImpl detachedInstance) {
        log.debug("merging AccountImpl instance");
        try {
            AccountImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public AccountImpl findById(int id) {
        log.debug("getting AccountImpl instance with id: " + id);
        try {
            AccountImpl instance = entityManager.find(AccountImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
