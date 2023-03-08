package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.DoubleRange;
import gms.shared.frameworks.configuration.constraints.NumericRangeConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.converter.OperatorDaoConverter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@DiscriminatorValue("numeric_range_constraint")
public class NumericRangeConstraintDao extends ConstraintDao {

  @Column(name = "numeric_range_min_value", nullable = false)
  private double minValue;

  @Column(name = "numeric_range_max_value", nullable = false)
  private double maxValue;

  @Override
  public Object getValue() {
    return DoubleRange.from(this.getMinValue(), this.getMaxValue());
  }

  @Override
  public void setValue(Object value) {
    if (!(value instanceof DoubleRange)) {
      throw new IllegalArgumentException("Object passed to DoubleRangeConstraintDao::setValue is not a DoubleRange");
    }
    var doubleRange = (DoubleRange) value;
    this.minValue = doubleRange.getMin();
    this.maxValue = doubleRange.getMax();
  }

  public double getMinValue() {
    return this.minValue;
  }

  public void setMinValue(double minValue) {
    this.minValue = minValue;
  }

  public double getMaxValue() {
    return this.maxValue;
  }

  public void setMaxValue(double maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public Constraint createConstraint() {
    return NumericRangeConstraint.from(
      this.getCriterion(),
      new OperatorDaoConverter().toCoi(this.getOperatorDao()),
      DoubleRange.from(this.getMinValue(), this.getMaxValue()),
      this.getPriority());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NumericRangeConstraintDao)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    NumericRangeConstraintDao that = (NumericRangeConstraintDao) o;
    return Double.compare(that.minValue, minValue) == 0 &&
      Double.compare(that.maxValue, maxValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), minValue, maxValue);
  }
}
