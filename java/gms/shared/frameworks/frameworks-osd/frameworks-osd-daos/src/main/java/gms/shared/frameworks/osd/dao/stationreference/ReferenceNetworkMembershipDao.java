package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reference_network_membership")
public class ReferenceNetworkMembershipDao {

  @Id
  @Column(unique = true)
  private UUID id;

  @Column(name = "comment")
  private String comment;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;


  @Column(name = "network_id")
  private UUID networkId;


  @Column(name = "station_id")
  private UUID stationId;

  @Column(name = "status")
  private StatusType status;

  /**
   * Default constructor for JPA.
   */
  public ReferenceNetworkMembershipDao() {
  }

  /**
   * Create a DAO from the given COI.
   *
   * @param membership The ReferenceNetworkMembership object.
   */
  public ReferenceNetworkMembershipDao(ReferenceNetworkMembership membership) {
    Objects.requireNonNull(membership);

    this.id = membership.getId();
    this.comment = membership.getComment();
    this.actualTime = membership.getActualChangeTime();
    this.systemTime = membership.getSystemChangeTime();
    this.networkId = membership.getNetworkId();
    this.stationId = membership.getStationId();
    this.status = membership.getStatus();
  }

  /**
   * Create a COI from this DAO.
   *
   * @return A ReferenceNetworkMembership object.
   */
  public ReferenceNetworkMembership toCoi() {
    return ReferenceNetworkMembership.from(getId(), getComment(), getActualTime(),
      getSystemTime(), getNetworkId(), getStationId(), getStatus());
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

  public UUID getNetworkId() {
    return networkId;
  }

  public void setNetworkId(UUID networkId) {
    this.networkId = networkId;
  }

  public UUID getStationId() {
    return stationId;
  }

  public void setStationId(UUID stationId) {
    this.stationId = stationId;
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
    ReferenceNetworkMembershipDao that = (ReferenceNetworkMembershipDao) o;
    return id.equals(that.id) &&
      comment.equals(that.comment) &&
      actualTime.equals(that.actualTime) &&
      systemTime.equals(that.systemTime) &&
      networkId.equals(that.networkId) &&
      stationId.equals(that.stationId) &&
      status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, comment, actualTime, systemTime, networkId, stationId, status);
  }

  @Override
  public String toString() {
    return "ReferenceNetworkMembershipDao{" +
      "id=" + id +
      ", comment='" + comment + '\'' +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      ", networkId=" + networkId +
      ", stationName=" + stationId +
      ", status=" + status +
      '}';
  }
}
