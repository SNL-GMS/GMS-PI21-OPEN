package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.TimeOfYearRange;
import gms.shared.frameworks.configuration.constraints.TimeOfYearRangeConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.converter.OperatorDaoConverter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@DiscriminatorValue("time_of_year_range_constraint")
public class TimeOfYearRangeConstraintDao extends ConstraintDao {

  @Column(name = "time_of_year_range_min_value", nullable = false)
  private LocalDateTime minValue;

  @Column(name = "time_of_year_range_max_value", nullable = false)
  private LocalDateTime maxValue;

  @Override
  public Object getValue() {
    return TimeOfYearRange.from(this.getMinValue(), this.getMaxValue());
  }

  @Override
  public void setValue(Object value) {
    if (!(value instanceof TimeOfYearRange)) {
      throw new IllegalArgumentException("Object passed to TimeOfYearRangeConstraintDao::setValue is not a TimeOfYearRange");
    }
    var timeOfYearRange = (TimeOfYearRange) value;
    this.minValue = timeOfYearRange.getMin();
    this.maxValue = timeOfYearRange.getMax();
  }

  public LocalDateTime getMinValue() {
    return this.minValue;
  }

  public void setMinValue(LocalDateTime minValue) {
    this.minValue = minValue;
  }

  public LocalDateTime getMaxValue() {
    return this.maxValue;
  }

  public void setMaxValue(LocalDateTime maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public Constraint createConstraint() {
    return TimeOfYearRangeConstraint.from(
      this.getCriterion(),
      new OperatorDaoConverter().toCoi(this.getOperatorDao()),
      TimeOfYearRange.from(this.getMinValue(), this.getMaxValue()),
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
    TimeOfYearRangeConstraintDao that = (TimeOfYearRangeConstraintDao) o;
    return Objects.equals(minValue, that.minValue) &&
      Objects.equals(maxValue, that.maxValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), minValue, maxValue);
  }
}
