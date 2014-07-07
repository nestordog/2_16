package ch.algotrader.entity.strategy;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class PortfolioValueImpl.
 * @see ch.algotrader.entity.strategy.PortfolioValueImpl
 * @author Hibernate Tools
 */
@Stateless
public class PortfolioValueImplHome {

    private static final Log log = LogFactory.getLog(PortfolioValueImplHome.class);

    @PersistenceContext private EntityManager entityManager;

    public void persist(PortfolioValueImpl transientInstance) {
        log.debug("persisting PortfolioValueImpl instance");
        try {
            entityManager.persist(transientInstance);
            log.debug("persist successful");
        } catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }

    public void remove(PortfolioValueImpl persistentInstance) {
        log.debug("removing PortfolioValueImpl instance");
        try {
            entityManager.remove(persistentInstance);
            log.debug("remove successful");
        } catch (RuntimeException re) {
            log.error("remove failed", re);
            throw re;
        }
    }

    public PortfolioValueImpl merge(PortfolioValueImpl detachedInstance) {
        log.debug("merging PortfolioValueImpl instance");
        try {
            PortfolioValueImpl result = entityManager.merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    public PortfolioValueImpl findById(int id) {
        log.debug("getting PortfolioValueImpl instance with id: " + id);
        try {
            PortfolioValueImpl instance = entityManager.find(PortfolioValueImpl.class, id);
            log.debug("get successful");
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
}
