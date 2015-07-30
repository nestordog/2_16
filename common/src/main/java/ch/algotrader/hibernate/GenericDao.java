package ch.algotrader.hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GenericDao {

    /**
     * gets any Entity by its {@code class} and {@code id}.
     * Securities will get initialzed. For {@link ch.algotrader.entity.security.Combination Combinations} all
     * {@link ch.algotrader.entity.security.Component Components} will get initialized.
     */
    public Object get(Class<?> clazz, Serializable id);

    /**
     * gets the initialized Collection specified by its {@code role} and entity {@code id}
     */
    public Object getInitializedCollection(String role, Serializable id);

    /**
     * Performs a HQL query based on the given {@code queryString}
     */
    public List<?> find(String queryString);

    /**
     * Performs a HQL query based on the given {@code queryString} and {@code maxResults}
     */
    public List<?> find(String queryString, int maxResults);

    /**
     * Performs a HQL query based on the given {@code queryString} and {@code namedParameters}
     * @return a List of Objects
     */
    public List<?> find(String queryString, Map<String, Object> namedParameters);

    /**
     * Performs a HQL query based on the given {@code queryString}, {@code namedParameters} and {@code maxResults}
     * @return a List of Objects
     */
    public List<?> find(String queryString, Map<String, Object> namedParameters, int maxResults);

    /**
     * Performs a HQL query based on the given {@code queryString}
     * @return a unique Object
     */
    public Object findUnique(String queryString);

    /**
     * Performs a HQL query based on the given {@code queryString} and {@code namedParameters}
     * @return a unique Object
     */
    public Object findUnique(String queryString, Map<String, Object> namedParameters);

    /**
     * Gets the querySpaces (tables) associated with a query
     */
    public Set<String> getQuerySpaces(String queryString);
}
