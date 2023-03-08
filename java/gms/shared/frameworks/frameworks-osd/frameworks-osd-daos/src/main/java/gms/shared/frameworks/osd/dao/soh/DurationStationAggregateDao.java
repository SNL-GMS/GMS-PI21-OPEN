package gms.shared.frameworks.osd.dao.soh;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.Duration;
import java.util.Objects;

@Entity
@DiscriminatorValue(value = "DURATION")
public class DurationStationAggregateDao extends StationAggregateDao {

  @Column(name = "duration")
  private Duration value;

  public Duration getValue() {
    return value;
  }

  public void setValue(Duration value) {
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
    DurationStationAggregateDao that = (DurationStationAggregateDao) o;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}
