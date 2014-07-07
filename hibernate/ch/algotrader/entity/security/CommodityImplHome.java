package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class CommodityImpl.
 * @see ch.algotrader.entity.security.CommodityImpl
 * @author Hibernate Tools
 */
@Stateless
public class CommodityImplHome {

    private static final Log log = LogFactory.getLog(CommodityImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(CommodityImpl transientInstance) {
        log.debug("persisting CommodityImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(CommodityImpl persistentInstance) {
        log.debug("removing CommodityImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public CommodityImpl merge(CommodityImpl detachedInstance) {
        log.debug("merging CommodityImpl instance");
        try {
            CommodityImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public CommodityImpl findById(int id) {
        log.debug("getting CommodityImpl instance with id: " + id);
        try {
            CommodityImpl instance = entityManager.find(CommodityImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
