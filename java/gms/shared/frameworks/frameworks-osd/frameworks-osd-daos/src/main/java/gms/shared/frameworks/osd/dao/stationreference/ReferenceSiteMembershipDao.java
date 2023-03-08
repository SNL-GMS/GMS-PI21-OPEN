package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reference_site_membership")
public class ReferenceSiteMembershipDao {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reference_site_membership_sequence")
  @SequenceGenerator(name = "reference_site_membership_sequence", sequenceName = "reference_site_membership_sequence", allocationSize = 5)
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name = "comment")
  private String comment;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  @Column(name = "site_id")
  private UUID siteId;

  @Column(name = "channel_name")
  private String channelName;

  @Column(name = "status")
  private StatusType status;

  /**
   * Default constructor for JPA.
   */
  public ReferenceSiteMembershipDao() {
  }

  /**
   * Create a DAO from the given COI.
   *
   * @param membership The ReferenceSiteMembership object.
   */
  public ReferenceSiteMembershipDao(ReferenceSiteMembership membership) {
    Objects.requireNonNull(membership);

    this.id = membership.getId();
    this.comment = membership.getComment();
    this.actualTime = membership.getActualChangeTime();
    this.systemTime = membership.getSystemChangeTime();
    this.siteId = membership.getSiteId();
    this.channelName = membership.getChannelName();
    this.status = membership.getStatus();
  }

  /**
   * Create a COI from this DAO.
   *
   * @return A ReferenceSiteMembership object.
   */
  public ReferenceSiteMembership toCoi() {
    return ReferenceSiteMembership.from(getId(), getComment(), getActualTime(),
      getSystemTime(), getSiteId(), getChannelName(), status);
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Instant getActualTime() {
    return actualTime;
  }

  public void setActualTime(Instant actualTime) {
    this.actualTime = actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(Instant systemTime) {
    this.systemTime = systemTime;
  }

  public UUID getSiteId() {
    return siteId;
  }

  public void setSiteId(UUID siteId) {
    this.siteId = siteId;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public StatusType getStatus() {
    return status;
  }

  public void setStatus(StatusType status) {
    this.status = status;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceSiteMembershipDao that = (ReferenceSiteMembershipDao) o;

    return new EqualsBuilder()
      .appendSuper(super.equals(o))
      .append(primaryKey, that.primaryKey)
      .append(id, that.id)
      .append(comment, that.comment)
      .append(actualTime, that.actualTime)
      .append(systemTime, that.systemTime)
      .append(siteId, that.siteId)
      .append(channelName, that.channelName)
      .append(status, that.status)
      .isEquals();
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (siteId != null ? siteId.hashCode() : 0);
    result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSiteMembershipDao{" +
      "primaryKey=" + primaryKey +
      ", id=" + id +
      ", comment='" + comment + '\'' +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      ", siteId=" + siteId +
      ", channelName=" + channelName +
      ", status=" + status +
      '}';
  }
}
