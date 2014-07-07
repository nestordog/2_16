package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class FutureFamilyImpl.
 * @see ch.algotrader.entity.security.FutureFamilyImpl
 * @author Hibernate Tools
 */
@Stateless
public class FutureFamilyImplHome {

    private static final Log log = LogFactory.getLog(FutureFamilyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(FutureFamilyImpl transientInstance) {
        log.debug("persisting FutureFamilyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(FutureFamilyImpl persistentInstance) {
        log.debug("removing FutureFamilyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public FutureFamilyImpl merge(FutureFamilyImpl detachedInstance) {
        log.debug("merging FutureFamilyImpl instance");
        try {
            FutureFamilyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public FutureFamilyImpl findById(int id) {
        log.debug("getting FutureFamilyImpl instance with id: " + id);
        try {
            FutureFamilyImpl instance = entityManager.find(FutureFamilyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
