package gms.shared.frameworks.osd.dao.soh;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@DiscriminatorValue(value = "PERCENT")
public class PercentStationAggregateDao extends StationAggregateDao {

  @Column(name = "percent")
  private Double value;

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
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

    PercentStationAggregateDao that = (PercentStationAggregateDao) o;

    /*
     *Checking for both items to be null - equal
     * Checking that both are not null and then the compare between those
     *  results in  not equal if only one is null otherwise it is the result of the two actual values
     */
    return ((this.value == null && that.value == null) ||
      (this.value != null && that.value != null && Double.compare(this.value, that.value) == 0));

  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}
