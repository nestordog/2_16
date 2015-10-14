/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/

package ch.algotrader.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
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

    @Override
    public Object nullSafeGet(
            final ResultSet resultSet, final String[] names, final SessionImplementor session, final Object owner) throws HibernateException, SQLException {
        final String name = resultSet.getString(names[0]);
        return resultSet.wasNull() ? null : Enum.valueOf(this.enumClass, name);
    }

    @Override
    public void nullSafeSet(
            final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws HibernateException, SQLException {
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
    @SuppressWarnings({"unchecked", "deprecation"})
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
    @SuppressWarnings({"unchecked", "deprecation"})
    public String toXMLString(Object value) {
        return ((Enum) value).name();
    }
}
