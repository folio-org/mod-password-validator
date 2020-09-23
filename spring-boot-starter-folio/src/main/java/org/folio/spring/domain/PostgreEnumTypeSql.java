package org.folio.spring.domain;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.EnhancedUserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Simple type to support PostgreSQL ENUM types
 */
public class PostgreEnumTypeSql implements EnhancedUserType {
  @Override
  public String objectToSQLString(Object value) {
    return Objects.nonNull(value) ? value.toString() : null;
  }

  @Override
  public String toXMLString(Object value) {
    return objectToSQLString(value);
  }

  @Override
  public Object fromXMLString(String xmlValue) {
    return xmlValue;
  }

  @Override
  public int[] sqlTypes() {
    return new int[]{Types.OTHER};
  }

  @Override
  public Class returnedClass() {
    return String.class;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return Objects.nonNull(x) && x.equals(y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return Objects.nonNull(x) ? x.hashCode() : 0;
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
    return rs.getString(names[0]);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.OTHER);
    } else {
      st.setObject(index, value.toString(), Types.OTHER);
    }
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return (value instanceof String) ? (Serializable) value : null;
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return cached;
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }

}
