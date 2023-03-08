package gms.shared.frameworks.osd.dao.soh.statuschange;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class SohStatusChangeDao {

  @Column(name = "first_change_time", nullable = false)
  private Instant firstChangeTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "soh_monitor_type", columnDefinition = "soh_monitor_type_enum", nullable = false)
  @Type(type = "pgsql_enum")
  private SohMonitorType sohMonitorType;

  @OneToOne
  @JoinColumn(
    name = "channel_name",
    referencedColumnName = "name",
    nullable = false
  )
  private ChannelDao changedChannel;

  public Instant getFirstChangeTime() {
    return firstChangeTime;
  }

  public void setFirstChangeTime(Instant changeTime) {
    this.firstChangeTime = changeTime;
  }

  public SohMonitorType getSohMonitorType() {
    return sohMonitorType;
  }

  public void setSohMonitorType(SohMonitorType sohMonitorType) {
    this.sohMonitorType = sohMonitorType;
  }

  public ChannelDao getChangedChannel() {
    return changedChannel;
  }

  public void setChangedChannel(
    ChannelDao changedChannel) {
    this.changedChannel = changedChannel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SohStatusChangeDao)) {
      return false;
    }
    SohStatusChangeDao that = (SohStatusChangeDao) o;
    return firstChangeTime.equals(that.firstChangeTime) &&
      sohMonitorType == that.sohMonitorType &&
      changedChannel.isChannelNameEqual(that.getChangedChannel());
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstChangeTime, sohMonitorType, changedChannel.getName());
  }


}
