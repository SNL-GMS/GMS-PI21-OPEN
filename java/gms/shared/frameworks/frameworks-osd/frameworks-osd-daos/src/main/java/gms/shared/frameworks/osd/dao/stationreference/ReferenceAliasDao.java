package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.stationreference.ReferenceAlias;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reference_alias")
public class ReferenceAliasDao {

  @Id
  @Column(name = "id", unique = true)
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "status")
  private StatusType status;

  @Column(name = "comment")
  private String comment;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  /**
   * Default constructor used by JPA.
   */
  public ReferenceAliasDao() {
  }

  /**
   * Create a new DAO from the corresponding COI object.
   *
   * @param referenceAlias The COI object to use.
   */
  public ReferenceAliasDao(ReferenceAlias referenceAlias) {
    Objects.requireNonNull(referenceAlias);
    this.id = referenceAlias.getId();
    this.name = referenceAlias.getName();
    this.status = referenceAlias.getStatus();
    this.comment = referenceAlias.getComment();
    this.actualTime = referenceAlias.getActualChangeTime();
    this.systemTime = referenceAlias.getSystemChangeTime();
  }

  /**
   * Convert this DAO into a COI object.
   *
   * @return A ReferenceAlias object.
   */
  public ReferenceAlias toCoi() {
    return ReferenceAlias.from(id, name, status, comment,
      actualTime, systemTime);
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StatusType getStatus() {
    return status;
  }

  public void setStatus(StatusType status) {
    this.status = status;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceAliasDao that = (ReferenceAliasDao) o;
    return id.equals(that.id) &&
      name.equals(that.name) &&
      status == that.status &&
      comment.equals(that.comment) &&
      actualTime.equals(that.actualTime) &&
      systemTime.equals(that.systemTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, status, comment, actualTime, systemTime);
  }

  @Override
  public String toString() {
    return "ReferenceAliasDao{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", status=" + status +
      ", comment='" + comment + '\'' +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      '}';
  }

}
