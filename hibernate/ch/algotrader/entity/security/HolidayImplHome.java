package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class HolidayImpl.
 * @see ch.algotrader.entity.security.HolidayImpl
 * @author Hibernate Tools
 */
@Stateless
public class HolidayImplHome {

    private static final Log log = LogFactory.getLog(HolidayImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(HolidayImpl transientInstance) {
        log.debug("persisting HolidayImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(HolidayImpl persistentInstance) {
        log.debug("removing HolidayImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public HolidayImpl merge(HolidayImpl detachedInstance) {
        log.debug("merging HolidayImpl instance");
        try {
            HolidayImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public HolidayImpl findById(int id) {
        log.debug("getting HolidayImpl instance with id: " + id);
        try {
            HolidayImpl instance = entityManager.find(HolidayImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
