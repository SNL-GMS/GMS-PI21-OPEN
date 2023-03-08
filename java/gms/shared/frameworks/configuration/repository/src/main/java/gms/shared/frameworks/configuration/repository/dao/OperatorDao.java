package gms.shared.frameworks.configuration.repository.dao;


import gms.shared.frameworks.configuration.Operator.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;

@Embeddable
public class OperatorDao {

  @Enumerated(EnumType.STRING)
  @Column(name = "operator_type", nullable = false)
  @org.hibernate.annotations.Type(type = "pgsql_enum")
  private Type type;

  @Column(name = "negated", nullable = false)
  private boolean negated;

  public Type getType() {
    return this.type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public boolean isNegated() {
    return this.negated;
  }

  public void setNegated(boolean negated) {
    this.negated = negated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    OperatorDao that = (OperatorDao) o;
    return this.negated == that.negated &&
      this.type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.type, this.negated);
  }
}
