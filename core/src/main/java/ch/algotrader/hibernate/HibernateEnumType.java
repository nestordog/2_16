package ch.algotrader.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

/**
 * A Hibernate UserType for Java5 enumerations. Taken from
 * <a href="http://www.hibernate.org/272.html">Java 5 EnumUserType</a>.
 */
public class HibernateEnumType implements EnhancedUserType, ParameterizedType {

    @SuppressWarnings("rawtypes") private Class<Enum> enumClass;

    /**
     * @see org.hibernate.usertype.ParameterizedType#setParameterValues(java.util.Properties)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setParameterValues(Properties parameters) {
        final String enumClassName = parameters.getProperty("enumClassName");
        try {
            //noinspection unchecked
            this.enumClass = (Class<Enum>) Class.forName(enumClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }
    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, Object)
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(Object)
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(Object)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Serializable disassemble(Object value) throws HibernateException {
        return (Enum) value;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(Object, Object)
     */
    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    /**
     * @see org.hibernate.usertype.UserType#hashCode(Object)
     */
    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /**
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
        final String name = resultSet.getString(names[0]);
        return resultSet.wasNull() ? null : Enum.valueOf(this.enumClass, name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            if (value instanceof Enum) {
                statement.setString(index, ((Enum) value).name());
            } else {
                statement.setString(index, (String) value);
            }
        }
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        return this.nullSafeGet(resultSet, names, owner);
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object data, int index, SessionImplementor session) throws HibernateException, SQLException {
        this.nullSafeSet(preparedStatement, data, index);
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(Object, Object, Object)
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    public Class<?> returnedClass() {
        return this.enumClass;
    }

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#fromXMLString(String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object fromXMLString(String xmlValue) {
        return Enum.valueOf(this.enumClass, xmlValue);
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#objectToSQLString(Object)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String objectToSQLString(Object value) {
        return '\'' + ((Enum) value).name() + '\'';
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#toXMLString(Object)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String toXMLString(Object value) {
        return ((Enum) value).name();
    }
}
