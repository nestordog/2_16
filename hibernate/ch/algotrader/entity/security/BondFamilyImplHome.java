package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class BondFamilyImpl.
 * @see ch.algotrader.entity.security.BondFamilyImpl
 * @author Hibernate Tools
 */
@Stateless
public class BondFamilyImplHome {

    private static final Log log = LogFactory.getLog(BondFamilyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(BondFamilyImpl transientInstance) {
        log.debug("persisting BondFamilyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(BondFamilyImpl persistentInstance) {
        log.debug("removing BondFamilyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public BondFamilyImpl merge(BondFamilyImpl detachedInstance) {
        log.debug("merging BondFamilyImpl instance");
        try {
            BondFamilyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public BondFamilyImpl findById(int id) {
        log.debug("getting BondFamilyImpl instance with id: " + id);
        try {
            BondFamilyImpl instance = entityManager.find(BondFamilyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
