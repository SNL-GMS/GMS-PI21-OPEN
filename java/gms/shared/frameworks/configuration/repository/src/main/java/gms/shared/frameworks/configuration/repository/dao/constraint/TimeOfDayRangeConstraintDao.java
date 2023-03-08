package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.TimeOfDayRange;
import gms.shared.frameworks.configuration.constraints.TimeOfDayRangeConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.converter.OperatorDaoConverter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@DiscriminatorValue("time_of_day_range_constraint")
public class TimeOfDayRangeConstraintDao extends ConstraintDao {

  @Column(name = "time_of_day_range_min_value", nullable = false)
  private LocalTime minValue;

  @Column(name = "time_of_day_range_max_value", nullable = false)
  private LocalTime maxValue;

  @Override
  public Object getValue() {
    return TimeOfDayRange.from(this.getMinValue(), this.getMaxValue());
  }

  @Override
  public void setValue(Object value) {
    if (!(value instanceof TimeOfDayRange)) {
      throw new IllegalArgumentException("Object passed to TimeOfDayRangeConstraintDao::setValue is not a TimeOfDayRange");
    }
    var timeOfDayRange = (TimeOfDayRange) value;
    this.minValue = timeOfDayRange.getMin();
    this.maxValue = timeOfDayRange.getMax();
  }

  public LocalTime getMinValue() {
    return this.minValue;
  }

  public void setMinValue(LocalTime minValue) {
    this.minValue = minValue;
  }

  public LocalTime getMaxValue() {
    return this.maxValue;
  }

  public void setMaxValue(LocalTime maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public Constraint createConstraint() {
    return TimeOfDayRangeConstraint.from(
      this.getCriterion(),
      new OperatorDaoConverter().toCoi(this.getOperatorDao()),
      TimeOfDayRange.from(this.getMinValue(), this.getMaxValue()),
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
    TimeOfDayRangeConstraintDao that = (TimeOfDayRangeConstraintDao) o;
    return Objects.equals(minValue, that.minValue) &&
      Objects.equals(maxValue, that.maxValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), minValue, maxValue);
  }
}
