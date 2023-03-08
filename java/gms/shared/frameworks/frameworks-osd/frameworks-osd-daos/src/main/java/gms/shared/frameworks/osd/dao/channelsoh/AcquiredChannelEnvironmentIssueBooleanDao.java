package gms.shared.frameworks.osd.dao.channelsoh;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Define a Data Access Object to allow access to the relational database.
 */
@Entity
@Table(name = "channel_env_issue_boolean")
public class AcquiredChannelEnvironmentIssueBooleanDao extends AcquiredChannelEnvironmentIssueDao {

  @Column(name = "status", nullable = false)
  private boolean status;

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public AcquiredChannelEnvironmentIssueBoolean toCoi() {
    return AcquiredChannelEnvironmentIssueBoolean.from(
      this.getChannelName(), this.getType(),
      this.getStartTime(), this.getEndTime(), this.status);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AcquiredChannelEnvironmentIssueBooleanDao)) return false;
    if (!super.equals(o)) return false;
    AcquiredChannelEnvironmentIssueBooleanDao that = (AcquiredChannelEnvironmentIssueBooleanDao) o;
    return isStatus() == that.isStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isStatus());
  }

  @Override
  public String toString() {
    return "AcquiredChannelEnvironmentIssueBooleanDao{" +
      "status=" + status +
      ", id=" + id +
      ", channelName='" + channelName + '\'' +
      ", type=" + type +
      ", startTime=" + startTime +
      ", endTime=" + endTime +
      '}';
  }
}
