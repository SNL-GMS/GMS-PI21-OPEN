package gms.shared.frameworks.osd.dao.soh;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "station_aggregate")
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public abstract class StationAggregateDao extends SohDao {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_aggregate_sequence")
  @SequenceGenerator(name = "station_aggregate_sequence", sequenceName = "station_aggregate_sequence")
  private long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "aggregate_type", nullable = false, columnDefinition = "public.station_aggregate_type_enum")
  @Type(type = "pgsql_enum")
  private StationAggregateType aggregateType;

  @JoinColumn(name = "station_soh_id", referencedColumnName = "id")
  @JoinColumn(name = "station_soh_station_name", referencedColumnName = "station_name")
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private StationSohDao stationSoh;

  protected StationAggregateDao() {
    // no-arg constructor
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public StationAggregateType getAggregateType() {
    return aggregateType;
  }

  public void setAggregateType(StationAggregateType aggregateType) {
    this.aggregateType = aggregateType;
  }

  public StationSohDao getStationSoh() {
    return stationSoh;
  }

  public void setStationSoh(StationSohDao stationSoh) {
    this.stationSoh = stationSoh;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationAggregateDao that = (StationAggregateDao) o;
    return getId() == that.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
