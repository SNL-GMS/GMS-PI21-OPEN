package gms.shared.frameworks.osd.dao.soh;

import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "channel_soh")
public class ChannelSohDao extends SohDao implements Serializable {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channel_soh_sequence")
  @SequenceGenerator(name = "channel_soh_sequence", sequenceName = "channel_soh_sequence", allocationSize = 50)
  private int id;

  @Column(name = "channel_name")
  private String channelName;

  @Enumerated(EnumType.STRING)
  @Column(name = "soh_status", nullable = false, columnDefinition = "public.soh_status_enum")
  @Type(type = "pgsql_enum")
  private SohStatus sohStatus;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "channelSoh")
  private Set<ChannelSohMonitorValueAndStatusDao> allMonitorValueAndStatuses;

  @JoinColumn(name = "station_soh_id", referencedColumnName = "id")
  @JoinColumn(name = "station_soh_station_name", referencedColumnName = "station_name")
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private StationSohDao stationSoh;

  @Column(name = "station_soh_station_name", insertable = false, updatable = false)
  private String stationName;

  public ChannelSohDao() {
    // No arg hibernate constructor
  }

  public String getChannelName() {
    return this.channelName;
  }

  public void setChannelName(final String channelName) {
    this.channelName = channelName;
  }

  public SohStatus getSohStatus() {
    return sohStatus;
  }

  public void setSohStatus(SohStatus sohStatus) {
    this.sohStatus = sohStatus;
  }

  public Set<ChannelSohMonitorValueAndStatusDao> getAllMonitorValueAndStatuses() {
    return allMonitorValueAndStatuses;
  }

  public void setAllMonitorValueAndStatuses(
    Set<ChannelSohMonitorValueAndStatusDao> allMonitorValueAndStatuses) {
    this.allMonitorValueAndStatuses = allMonitorValueAndStatuses;
  }

  public StationSohDao getStationSoh() {
    return stationSoh;
  }

  public void setStationSoh(StationSohDao stationSoh) {
    this.stationSoh = stationSoh;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChannelSohDao that = (ChannelSohDao) o;
    return getChannelName().equals(that.getChannelName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelName);
  }
}
