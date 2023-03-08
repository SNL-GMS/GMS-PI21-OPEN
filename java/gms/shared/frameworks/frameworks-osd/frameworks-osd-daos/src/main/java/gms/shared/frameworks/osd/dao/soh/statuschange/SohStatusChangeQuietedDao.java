package gms.shared.frameworks.osd.dao.soh.statuschange;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channel.StationDao;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Entity
@NamedQuery(name = "SohStatusChangeQuietedDao.checkExistsByChannelNameAndMonitorType",
  query = "SELECT dao FROM SohStatusChangeQuietedDao dao WHERE dao.channel.name = :channelName AND dao.sohMonitorType = :sohMonitorType")
@Table(name = "soh_status_change_quieted",
  uniqueConstraints = {@UniqueConstraint(columnNames = {"soh_monitor_type", "channel_name"})})
public class SohStatusChangeQuietedDao {

  @Id
  @Column(name = "soh_status_change_quieted_id", unique = true, updatable = false, nullable = false)
  private UUID quietedSohStatusChangeId;

  @Column(name = "quiet_until", nullable = false)
  private Instant quietUntil;

  @Column(name = "quiet_duration", nullable = false)
  private Duration quietDuration;

  @Enumerated(EnumType.STRING)
  @Column(name = "soh_monitor_type", columnDefinition = "soh_monitor_type_enum", nullable = false)
  @Type(type = "pgsql_enum")
  private SohMonitorType sohMonitorType;

  @Column(name = "comment", nullable = true, length = 1024)
  private String comment;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_name", referencedColumnName = "name", nullable = false)
  private ChannelDao channel;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "station_name", referencedColumnName = "name", nullable = false)
  private StationDao station;

  public UUID getQuietedSohStatusChangeId() {
    return this.quietedSohStatusChangeId;
  }

  public void setQuietedSohStatusChangeId(final UUID quietedSohStatusChangeId) {
    this.quietedSohStatusChangeId = quietedSohStatusChangeId;
  }


  public Instant getQuietUntil() {
    return this.quietUntil;
  }

  public void setQuietUntil(final Instant quietUntil) {
    this.quietUntil = quietUntil;
  }

  public Duration getQuietDuration() {
    return this.quietDuration;
  }

  public void setQuietDuration(Duration quietDuration) {
    this.quietDuration = quietDuration;
  }

  public String getComment() {
    return this.comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public ChannelDao getChannel() {
    return this.channel;
  }

  public String getChannelName() {
    return this.channel.getName();
  }

  public void setChannel(final ChannelDao channel) {
    this.channel = channel;
  }

  public String getStationName() {
    return this.station.getName();
  }

  public void setStation(StationDao stationDao) {
    this.station = stationDao;
  }

  public SohMonitorType getSohMonitorType() {
    return this.sohMonitorType;
  }


  public void setSohMonitorType(final SohMonitorType sohMonitorType) {
    this.sohMonitorType = sohMonitorType;
  }
}
