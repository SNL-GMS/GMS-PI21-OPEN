package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.NumericScalarConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.converter.OperatorDaoConverter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@DiscriminatorValue("numeric_scalar_constraint")
public class NumericScalarConstraintDao extends ConstraintDao {

  @Column(name = "numeric_scalar_value", nullable = false)
  private Double value;

  @Override
  public Double getValue() {
    return this.value;
  }

  @Override
  public void setValue(Object value) {
    if (!(value instanceof Double)) {
      throw new IllegalArgumentException("Object passed to NumericScalarConstraintDao::setValue is not a Double");
    }

    this.value = (Double) value;
  }

  @Override
  public Constraint createConstraint() {
    return NumericScalarConstraint.from(
      this.getCriterion(),
      new OperatorDaoConverter().toCoi(this.getOperatorDao()),
      this.getValue(),
      this.getPriority());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    NumericScalarConstraintDao that = (NumericScalarConstraintDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}
