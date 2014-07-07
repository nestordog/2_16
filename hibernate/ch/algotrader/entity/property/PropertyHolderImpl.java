package ch.algotrader.entity.property;

// Generated Jul 5, 2014 4:17:33 PM by Hibernate Tools 3.4.0.CR1

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.GenericGenerator;

/**
 * Base class of an Entity that can hold {@link Property Properties}.
 */
@Entity
@Table(name = "property_holder")
public class PropertyHolderImpl implements java.io.Serializable {

    private int id;
    private int version;
    /**
     * Base class of an Entity that can hold {@link Property Properties}.
    */
    private Map<String, PropertyImpl> props = new HashMap<String, PropertyImpl>(0);

    public PropertyHolderImpl() {
    }

    public PropertyHolderImpl(Map<String, PropertyImpl> props) {
        this.props = props;
    }

    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "ID", nullable = false, columnDefinition = "INTEGER")
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION", nullable = false)
    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     *      * Base class of an Entity that can hold {@link Property Properties}.
     */

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, mappedBy = "propertyHolder")
    public Map<String, PropertyImpl> getProps() {
        return this.props;
    }

    public void setProps(Map<String, PropertyImpl> props) {
        this.props = props;
    }

}
