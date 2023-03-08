package gms.shared.frameworks.osd.dao.soh;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "channel_soh_monitor_value_status")
public class ChannelSohMonitorValueAndStatusDao extends SohMonitorValueAndStatusDao {

  public ChannelSohMonitorValueAndStatusDao() {
    //empty constructor for JPA
  }


  @JoinColumn(name = "channel_soh_id", referencedColumnName = "id")
  @JoinColumn(name = "station_name", referencedColumnName = "station_soh_station_name")
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private ChannelSohDao channelSoh;

  @Column(name = "channel_name")
  private String channelName;
  
  @Column(name = "station_name", insertable = false, updatable = false)
  private String stationName;

  public ChannelSohDao getChannelSoh() {
    return channelSoh;
  }

  public void setChannelSoh(ChannelSohDao channelSoh) {
    this.channelSoh = channelSoh;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ChannelSohMonitorValueAndStatusDao that = (ChannelSohMonitorValueAndStatusDao) o;
    return Objects.equals(channelSoh, that.channelSoh) && 
      Objects.equals(stationName, that.stationName) && 
      Objects.equals(channelName, that.channelName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), channelSoh, stationName, channelName);
  }
}
