package gms.shared.frameworks.osd.dao.transferredfile;

import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

@Entity
@Table(name = "waveform_summary")
public class WaveformSummaryDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "waveform_summary_sequence")
  @SequenceGenerator(name = "waveform_summary_sequence", sequenceName = "waveform_summary_sequence", allocationSize = 5)
  private long id;

  @Column(name = "channel_name", nullable = false)
  private String channelName;

  @Column(name = "start_time", nullable = false)
  private Instant startTime;

  @Column(name = "end_time", nullable = false)
  private Instant endTime;

  /**
   * Default no-arg constructor (for use by JPA)
   */
  public WaveformSummaryDao() {
  }

  private WaveformSummaryDao(String channelName, Instant startTime, Instant endTime) {
    checkArgument(!endTime.isBefore(startTime), "End time cannot be before Start time");

    this.channelName = Objects.requireNonNull(channelName);
    this.startTime = Objects.requireNonNull(startTime);
    this.endTime = Objects.requireNonNull(endTime);
  }

  public WaveformSummary toCoi() {
    return WaveformSummary.from(this.channelName, this.startTime, this.endTime);
  }

  public static WaveformSummaryDao from(WaveformSummary waveformSummary) {
    return new WaveformSummaryDao(waveformSummary.getChannelName(), waveformSummary.getStartTime(),
      waveformSummary.getEndTime());
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WaveformSummary that = (WaveformSummary) o;
    return Objects.equals(channelName, that.getChannelName()) &&
      Objects.equals(startTime, that.getStartTime()) &&
      Objects.equals(endTime, that.getEndTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelName, startTime, endTime);
  }

  @Override
  public String toString() {
    return "WaveformSummary{" +
      "channelName=" + channelName +
      ", startTime=" + startTime +
      ", endTime=" + endTime +
      '}';
  }
}
