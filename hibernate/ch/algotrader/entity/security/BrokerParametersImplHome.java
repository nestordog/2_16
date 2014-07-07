package ch.algotrader.entity.security;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class BrokerParametersImpl.
 * @see ch.algotrader.entity.security.BrokerParametersImpl
 * @author Hibernate Tools
 */
@Stateless
public class BrokerParametersImplHome {

    private static final Log log = LogFactory.getLog(BrokerParametersImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(BrokerParametersImpl transientInstance) {
        log.debug("persisting BrokerParametersImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(BrokerParametersImpl persistentInstance) {
        log.debug("removing BrokerParametersImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public BrokerParametersImpl merge(BrokerParametersImpl detachedInstance) {
        log.debug("merging BrokerParametersImpl instance");
        try {
            BrokerParametersImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public BrokerParametersImpl findById(int id) {
        log.debug("getting BrokerParametersImpl instance with id: " + id);
        try {
            BrokerParametersImpl instance = entityManager.find(BrokerParametersImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
