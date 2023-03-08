package gms.shared.frameworks.osd.dao.channelsoh;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.stationgroupsoh.converter.AcquiredChannelEnvironmentIssueTypeConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public class AcquiredChannelEnvironmentIssueDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channel_env_issue_sequence")
  @SequenceGenerator(name = "channel_env_issue_sequence", sequenceName = "channel_env_issue_sequence",
    allocationSize = 1)
  protected long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_name", referencedColumnName = "name")
  protected ChannelDao channel;

  //this allows us to retrieve the channel_name without an extraneous join
  @Column(name = "channel_name", insertable = false, updatable = false)
  protected String channelName;

  @Column(name = "type", nullable = false)
  @Convert(converter = AcquiredChannelEnvironmentIssueTypeConverter.class)
  protected AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType type;

  @Column(name = "start_time", nullable = false)
  protected Instant startTime;

  @Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  protected Instant endTime;

  public AcquiredChannelEnvironmentIssueDao() {
    // Empty constructor needed for JPA
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public ChannelDao getChannel() {
    return channel;
  }

  public void setChannel(ChannelDao channel) {
    this.channel = channel;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType getType() {
    return type;
  }

  public void setType(
    AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType type) {
    this.type = type;
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
    if (this == o) return true;
    if (!(o instanceof AcquiredChannelEnvironmentIssueDao)) return false;
    AcquiredChannelEnvironmentIssueDao that = (AcquiredChannelEnvironmentIssueDao) o;
    return getChannelName().equals(that.getChannelName()) &&
      getType() == that.getType() &&
      getStartTime().equals(that.getStartTime()) &&
      getEndTime().equals(that.getEndTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getChannelName(), getType(), getStartTime(), getEndTime());
  }

  @Override
  public String toString() {
    return "AcquiredChannelEnvironmentIssueDao{" +
      "id=" + id +
      ", channelName='" + channelName + '\'' +
      ", type=" + type +
      ", startTime=" + startTime +
      ", endTime=" + endTime +
      '}';
  }
}
