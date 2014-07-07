package ch.algotrader.entity.marketData;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class GenericTickImpl.
 * @see ch.algotrader.entity.marketData.GenericTickImpl
 * @author Hibernate Tools
 */
@Stateless
public class GenericTickImplHome {

    private static final Log log = LogFactory.getLog(GenericTickImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(GenericTickImpl transientInstance) {
        log.debug("persisting GenericTickImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(GenericTickImpl persistentInstance) {
        log.debug("removing GenericTickImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public GenericTickImpl merge(GenericTickImpl detachedInstance) {
        log.debug("merging GenericTickImpl instance");
        try {
            GenericTickImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public GenericTickImpl findById(int id) {
        log.debug("getting GenericTickImpl instance with id: " + id);
        try {
            GenericTickImpl instance = entityManager.find(GenericTickImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
