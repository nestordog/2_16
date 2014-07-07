package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class ImpliedVolatilityImpl.
 * @see ch.algotrader.entity.security.ImpliedVolatilityImpl
 * @author Hibernate Tools
 */
@Stateless
public class ImpliedVolatilityImplHome {

    private static final Log log = LogFactory.getLog(ImpliedVolatilityImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(ImpliedVolatilityImpl transientInstance) {
        log.debug("persisting ImpliedVolatilityImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(ImpliedVolatilityImpl persistentInstance) {
        log.debug("removing ImpliedVolatilityImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public ImpliedVolatilityImpl merge(ImpliedVolatilityImpl detachedInstance) {
        log.debug("merging ImpliedVolatilityImpl instance");
        try {
            ImpliedVolatilityImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public ImpliedVolatilityImpl findById(int id) {
        log.debug("getting ImpliedVolatilityImpl instance with id: " + id);
        try {
            ImpliedVolatilityImpl instance = entityManager.find(ImpliedVolatilityImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
