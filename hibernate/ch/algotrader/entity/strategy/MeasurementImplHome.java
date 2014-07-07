package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class MeasurementImpl.
 * @see ch.algotrader.entity.strategy.MeasurementImpl
 * @author Hibernate Tools
 */
@Stateless
public class MeasurementImplHome {

    private static final Log log = LogFactory.getLog(MeasurementImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(MeasurementImpl transientInstance) {
        log.debug("persisting MeasurementImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(MeasurementImpl persistentInstance) {
        log.debug("removing MeasurementImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public MeasurementImpl merge(MeasurementImpl detachedInstance) {
        log.debug("merging MeasurementImpl instance");
        try {
            MeasurementImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public MeasurementImpl findById(int id) {
        log.debug("getting MeasurementImpl instance with id: " + id);
        try {
            MeasurementImpl instance = entityManager.find(MeasurementImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
