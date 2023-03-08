package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.BooleanConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@DiscriminatorValue("boolean_constraint")
public class BooleanConstraintDao extends ConstraintDao {

  @Column(name = "boolean_value", nullable = false)
  private Boolean value;

  @Override
  public Boolean getValue() {
    return this.value;
  }


  @Override
  public void setValue(Object value) {
    if (!(value instanceof Boolean)) {
      throw new IllegalArgumentException("Object passed to BooleanConstraintDao::setValue is not a Boolean");
    }
    this.value = (Boolean) value;
  }

  @Override
  public Constraint createConstraint() {
    return BooleanConstraint.from(
      this.getCriterion(),
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
    BooleanConstraintDao that = (BooleanConstraintDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}
