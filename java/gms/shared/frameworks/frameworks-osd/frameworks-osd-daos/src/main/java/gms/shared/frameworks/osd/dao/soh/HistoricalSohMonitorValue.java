package gms.shared.frameworks.osd.dao.soh;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

import java.time.Instant;

/**
 * HistoricalSohMonitorValue is a POJO intended to hold the values returned from the PerformanceMonitoringRepositoryJpa
 * queryHistoricalStationSoh query.
 */
public class HistoricalSohMonitorValue {

  /**
   * Constructor for a HistoricalSohMonitorValue object
   *
   * @param stationName The stationName
   * @param channelName The channelName
   * @param creationTime The creationTime
   * @param monitorType The monitorType
   */
  public HistoricalSohMonitorValue(String stationName, String channelName, Instant creationTime,
    SohMonitorType monitorType) {
    this.stationName = stationName;
    this.channelName = channelName;
    this.creationTime = creationTime;
    this.monitorType = monitorType;
  }

  private Float percent;

  private Integer duration;

  private SohStatus status;

  private final String stationName;

  private final String channelName;

  private final Instant creationTime;

  private final SohMonitorType monitorType;

  /**
   * Gets the station name
   *
   * @return the station name
   */
  public String getStationName() {
    return stationName;
  }

  /**
   * Gets the channel name
   *
   * @return the channel name
   */
  public String getChannelName() {
    return channelName;
  }

  /**
   * Get the creation time
   *
   * @return the creation time
   */
  public Instant getCreationTime() {
    return creationTime;
  }

  /**
   * Gets the monitor type
   *
   * @return the monitor type
   */
  public SohMonitorType getMonitorType() {
    return monitorType;
  }

  /**
   * Gets the currently set percent
   *
   * @return the percent
   */
  public Float getPercent() {
    return percent;
  }

  /**
   * Sets the percent
   *
   * @param percent the percent to set
   */
  public void setPercent(Float percent) {
    this.percent = percent;
  }

  /**
   * Gets the currently set Duration
   *
   * @return the Duration
   */
  public Integer getDuration() {
    return duration;
  }

  /**
   * Sets the duration
   *
   * @param duration the duration to set
   */
  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  /**
   * Gets the currently set status
   *
   * @return the status
   */
  public SohStatus getStatus() {
    return status;
  }

  /**
   * Sets the status
   *
   * @param status the status
   */
  public void setStatus(SohStatus status) {
    this.status = status;
  }

}
