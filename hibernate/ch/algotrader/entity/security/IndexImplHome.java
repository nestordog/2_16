package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class IndexImpl.
 * @see ch.algotrader.entity.security.IndexImpl
 * @author Hibernate Tools
 */
@Stateless
public class IndexImplHome {

    private static final Log log = LogFactory.getLog(IndexImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(IndexImpl transientInstance) {
        log.debug("persisting IndexImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(IndexImpl persistentInstance) {
        log.debug("removing IndexImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public IndexImpl merge(IndexImpl detachedInstance) {
        log.debug("merging IndexImpl instance");
        try {
            IndexImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public IndexImpl findById(int id) {
        log.debug("getting IndexImpl instance with id: " + id);
        try {
            IndexImpl instance = entityManager.find(IndexImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
