package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class SecurityFamilyImpl.
 * @see ch.algotrader.entity.security.SecurityFamilyImpl
 * @author Hibernate Tools
 */
@Stateless
public class SecurityFamilyImplHome {

    private static final Log log = LogFactory.getLog(SecurityFamilyImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(SecurityFamilyImpl transientInstance) {
        log.debug("persisting SecurityFamilyImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(SecurityFamilyImpl persistentInstance) {
        log.debug("removing SecurityFamilyImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public SecurityFamilyImpl merge(SecurityFamilyImpl detachedInstance) {
        log.debug("merging SecurityFamilyImpl instance");
        try {
            SecurityFamilyImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public SecurityFamilyImpl findById(int id) {
        log.debug("getting SecurityFamilyImpl instance with id: " + id);
        try {
            SecurityFamilyImpl instance = entityManager.find(SecurityFamilyImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
