package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Contains information on the use of arrivals and origins in the GA application.
 */
@Entity
@Table(name = "ga_tag")
public class GaTagDao {
  private String objectType; // Type of identifier (a for arrival, o for origin)
  private long id; // ID number (arid or orid)
  private String processState; // Use of arid or orid
  private double latitude;
  private double longitude;
  private double time;
  private long rejectedArrivalOriginEvid; // Evid of origin that arrival was rejected from
  private String author;
  private Instant loadDate;

  public GaTagDao() {

  }

  private GaTagDao(Builder builder) {
    this.objectType = builder.objectType;
    this.id = builder.id;
    this.processState = builder.processState;
    this.latitude = builder.latitude;
    this.longitude = builder.longitude;
    this.time = builder.time;
    this.rejectedArrivalOriginEvid = builder.rejectedArrivalOriginEvid;
    this.author = builder.author;
    this.loadDate = builder.loadDate;
  }

  @Column(name = "objtype")
  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  @Id
  @Column(name = "id")
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Column(name = "process_state")
  public String getProcessState() {
    return processState;
  }

  public void setProcessState(String processState) {
    this.processState = processState;
  }

  @Column(name = "lat")
  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Column(name = "lon")
  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Column(name = "time")
  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  @Column(name = "evid_reject")
  public long getRejectedArrivalOriginEvid() {
    return rejectedArrivalOriginEvid;
  }

  public void setRejectedArrivalOriginEvid(long rejectedArrivalOriginEvid) {
    this.rejectedArrivalOriginEvid = rejectedArrivalOriginEvid;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  public static class Builder {
    private String objectType;
    private long id;
    private String processState;
    private double latitude;
    private double longitude;
    private double time;
    private long rejectedArrivalOriginEvid;
    private String author;
    private Instant loadDate;

    public static GaTagDao.Builder initializeFromInstance(GaTagDao gaTagDao) {
      return new GaTagDao.Builder()
        .withAuthor(gaTagDao.getAuthor())
        .withId(gaTagDao.getId())
        .withObjectType(gaTagDao.getObjectType())
        .withLatitude(gaTagDao.getLatitude())
        .withLongitude(gaTagDao.getLongitude())
        .withTime(gaTagDao.getTime())
        .withLoadDate(gaTagDao.getLoadDate())
        .withRejectedArrivalOriginEvid(gaTagDao.getRejectedArrivalOriginEvid())
        .withProcessState(gaTagDao.getProcessState());
    }

    public Builder withObjectType(String objectType) {
      this.objectType = objectType;
      return this;
    }

    public Builder withId(long id) {
      this.id = id;
      return this;
    }

    public Builder withProcessState(String processState) {
      this.processState = processState;
      return this;
    }

    public Builder withLatitude(double latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder withLongitude(double longitude) {
      this.longitude = longitude;
      return this;
    }

    public Builder withTime(double time) {
      this.time = time;
      return this;
    }

    public Builder withRejectedArrivalOriginEvid(long rejectedArrivalOriginEvid) {
      this.rejectedArrivalOriginEvid = rejectedArrivalOriginEvid;
      return this;
    }

    public Builder withAuthor(String author) {
      this.author = author;
      return this;
    }

    public Builder withLoadDate(Instant loadDate) {
      this.loadDate = loadDate;
      return this;
    }

    public GaTagDao build() {
      return new GaTagDao(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GaTagDao)) return false;
    var gaTagDao = (GaTagDao) o;
    return getId() == gaTagDao.getId()
      && Double.compare(gaTagDao.getLatitude(), getLatitude()) == 0
      && Double.compare(gaTagDao.getLongitude(), getLongitude()) == 0
      && Double.compare(gaTagDao.getTime(), getTime()) == 0
      && getRejectedArrivalOriginEvid() == gaTagDao.getRejectedArrivalOriginEvid()
      && getObjectType().equals(gaTagDao.getObjectType())
      && getProcessState().equals(gaTagDao.getProcessState())
      && getAuthor().equals(gaTagDao.getAuthor())
      && getLoadDate().equals(gaTagDao.getLoadDate());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getObjectType(), getId(), getProcessState(), getLatitude(), getLongitude(), getTime(),
      getRejectedArrivalOriginEvid(), getAuthor(), getLoadDate());
  }

  @Override
  public String toString() {
    return "GaTagDao{" +
      "objectType='" + objectType + '\'' +
      ", id=" + id +
      ", processState='" + processState + '\'' +
      ", latitude=" + latitude +
      ", longitude=" + longitude +
      ", time=" + time +
      ", rejectedArrivalOriginEvid=" + rejectedArrivalOriginEvid +
      ", author='" + author + '\'' +
      ", loadDate=" + loadDate +
      '}';
  }
}
