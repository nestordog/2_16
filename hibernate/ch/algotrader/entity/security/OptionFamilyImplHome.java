package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class OptionFamilyImpl.
 * @see ch.algotrader.entity.security.OptionFamilyImpl
 * @author Hibernate Tools
 */
@Stateless
public class OptionFamilyImplHome {

    private static final Log log = LogFactory.getLog(OptionFamilyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(OptionFamilyImpl transientInstance) {
        log.debug("persisting OptionFamilyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(OptionFamilyImpl persistentInstance) {
        log.debug("removing OptionFamilyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public OptionFamilyImpl merge(OptionFamilyImpl detachedInstance) {
        log.debug("merging OptionFamilyImpl instance");
        try {
            OptionFamilyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public OptionFamilyImpl findById(int id) {
        log.debug("getting OptionFamilyImpl instance with id: " + id);
        try {
            OptionFamilyImpl instance = entityManager.find(OptionFamilyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
