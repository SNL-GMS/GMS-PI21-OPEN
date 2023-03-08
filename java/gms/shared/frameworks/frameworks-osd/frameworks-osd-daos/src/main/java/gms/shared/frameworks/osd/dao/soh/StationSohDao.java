package gms.shared.frameworks.osd.dao.soh;

import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "station_soh")
@IdClass(StationSohId.class)
@org.hibernate.annotations.Cache(
  usage = CacheConcurrencyStrategy.READ_WRITE
)
@NaturalIdCache
@NamedQueries(
  @NamedQuery(name = "StationSohDao.exists",
    query = "SELECT CASE WHEN COUNT(dao) > 0 THEN 1 ELSE 0 END FROM StationSohDao dao WHERE dao.coiId = :coiId")
)
public class StationSohDao extends SohDao implements Serializable {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_soh_sequence")
  @SequenceGenerator(name = "station_soh_sequence", sequenceName = "station_soh_sequence", allocationSize = 10)
  private int id;

  @NaturalId
  @Column(name = "coi_id", updatable = false, nullable = false)
  private UUID coiId;

  @Id
  @Column(name = "station_name", nullable = false, updatable = false)
  private String stationName;

  @Enumerated(EnumType.STRING)
  @Column(name = "soh_status", nullable = false, columnDefinition = "public.soh_status_enum")
  @Type(type = "pgsql_enum")
  private SohStatus sohStatus;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "stationSoh")
  private Set<StationSohMonitorValueAndStatusDao> sohMonitorValueAndStatuses;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "stationSoh")
  private Set<StationAggregateDao> allStationAggregate;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "stationSoh")
  private Set<ChannelSohDao> channelSohs;

  public StationSohDao() {
    // empty JPA constructor
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public UUID getCoiId() {
    return this.coiId;
  }

  public void setCoiId(final UUID coiId) {
    this.coiId = coiId;
  }

  public SohStatus getSohStatus() {
    return sohStatus;
  }

  public void setSohStatus(SohStatus sohStatus) {
    this.sohStatus = sohStatus;
  }

  public String getStationName() {
    return this.stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  public Set<StationSohMonitorValueAndStatusDao> getSohMonitorValueAndStatuses() {
    return this.sohMonitorValueAndStatuses;
  }

  public void setSohMonitorValueAndStatuses(
    final Set<StationSohMonitorValueAndStatusDao> sohMonitorValueAndStatuses) {
    this.sohMonitorValueAndStatuses = sohMonitorValueAndStatuses;
  }

  public Set<StationAggregateDao> getAllStationAggregate() {
    return allStationAggregate;
  }

  public void setAllStationAggregate(
    Set<StationAggregateDao> allStationAggregate) {
    this.allStationAggregate = allStationAggregate;
  }

  public Set<ChannelSohDao> getChannelSohs() {
    return channelSohs;
  }

  public void setChannelSohs(Set<ChannelSohDao> channelSohs) {
    this.channelSohs = channelSohs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationSohDao that = (StationSohDao) o;
    return getId() == that.getId() && getStationName().equals(that.getStationName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getStationName());
  }
}
