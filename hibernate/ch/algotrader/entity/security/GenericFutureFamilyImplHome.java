package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class GenericFutureFamilyImpl.
 * @see ch.algotrader.entity.security.GenericFutureFamilyImpl
 * @author Hibernate Tools
 */
@Stateless
public class GenericFutureFamilyImplHome {

    private static final Log log = LogFactory.getLog(GenericFutureFamilyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(GenericFutureFamilyImpl transientInstance) {
        log.debug("persisting GenericFutureFamilyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(GenericFutureFamilyImpl persistentInstance) {
        log.debug("removing GenericFutureFamilyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public GenericFutureFamilyImpl merge(GenericFutureFamilyImpl detachedInstance) {
        log.debug("merging GenericFutureFamilyImpl instance");
        try {
            GenericFutureFamilyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public GenericFutureFamilyImpl findById(int id) {
        log.debug("getting GenericFutureFamilyImpl instance with id: " + id);
        try {
            GenericFutureFamilyImpl instance = entityManager.find(GenericFutureFamilyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
