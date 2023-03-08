package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.dao.util.UnitsConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class DoubleValueDao {

  // These fields are Double objects so that they can be null,
  // because classes that use this may need to be null also.

  @Column(name = "value")
  private Double value;

  @Column(name = "standard_deviation")
  private Double standardDeviation;

  @Column(name = "units")
  @Convert(converter = UnitsConverter.class)
  private Units units;

  public DoubleValueDao() {
  }

  public DoubleValueDao(DoubleValue val) {
    Objects.requireNonNull(val, "Cannot create DoubleValueDao from null DoubleValue");
    this.value = val.getValue();
    this.standardDeviation = val.getStandardDeviation();
    this.units = val.getUnits();
  }

  public DoubleValue toCoi() {
    return DoubleValue.from(this.value, this.standardDeviation, this.units);
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public double getStandardDeviation() {
    return standardDeviation;
  }

  public void setStandardDeviation(double standardDeviation) {
    this.standardDeviation = standardDeviation;
  }

  public Units getUnits() {
    return units;
  }

  public void setUnits(Units units) {
    this.units = units;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DoubleValueDao that = (DoubleValueDao) o;
    return Double.compare(that.value, value) == 0 &&
      Double.compare(that.standardDeviation, standardDeviation) == 0 &&
      units == that.units;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, standardDeviation, units);
  }

  @Override
  public String toString() {
    return "DoubleValueDao{" +
      "value=" + value +
      ", standardDeviation=" + standardDeviation +
      ", units=" + units +
      '}';
  }
}
