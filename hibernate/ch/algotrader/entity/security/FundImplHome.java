package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class FundImpl.
 * @see ch.algotrader.entity.security.FundImpl
 * @author Hibernate Tools
 */
@Stateless
public class FundImplHome {

    private static final Log log = LogFactory.getLog(FundImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(FundImpl transientInstance) {
        log.debug("persisting FundImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(FundImpl persistentInstance) {
        log.debug("removing FundImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public FundImpl merge(FundImpl detachedInstance) {
        log.debug("merging FundImpl instance");
        try {
            FundImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public FundImpl findById(int id) {
        log.debug("getting FundImpl instance with id: " + id);
        try {
            FundImpl instance = entityManager.find(FundImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
