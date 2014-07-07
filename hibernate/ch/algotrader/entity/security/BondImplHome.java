package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class BondImpl.
 * @see ch.algotrader.entity.security.BondImpl
 * @author Hibernate Tools
 */
@Stateless
public class BondImplHome {

    private static final Log log = LogFactory.getLog(BondImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(BondImpl transientInstance) {
        log.debug("persisting BondImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(BondImpl persistentInstance) {
        log.debug("removing BondImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public BondImpl merge(BondImpl detachedInstance) {
        log.debug("merging BondImpl instance");
        try {
            BondImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public BondImpl findById(int id) {
        log.debug("getting BondImpl instance with id: " + id);
        try {
            BondImpl instance = entityManager.find(BondImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
