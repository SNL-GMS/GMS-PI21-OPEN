package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a record in the `netmag` legacy table.
 */
@Entity
@Table(name = "netmag")
public class NetMagDao {

  private long magnitudeId;
  private String network;
  private long originId;
  private long eventId;
  private String magnitudeType;
  private int numberOfStations;
  private double magnitude;
  private double magnitudeUncertainty;
  private String author;
  private long commentId;
  private Instant loadDate;

  public NetMagDao() {
  }

  private NetMagDao(Builder builder) {
    this.magnitudeId = builder.magnitudeId;
    this.network = builder.network;
    this.originId = builder.originId;
    this.eventId = builder.eventId;
    this.magnitudeType = builder.magnitudeType;
    this.numberOfStations = builder.numberOfStations;
    this.magnitude = builder.magnitude;
    this.magnitudeUncertainty = builder.magnitudeUncertainty;
    this.author = builder.author;
    this.commentId = builder.commentId;
    this.loadDate = builder.loadDate;
  }

  @Id
  @Column(name = "magid")
  public long getMagnitudeId() {
    return magnitudeId;
  }

  public void setMagnitudeId(long magnitudeId) {
    this.magnitudeId = magnitudeId;
  }

  @Column(name = "net")
  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  @Column(name = "orid")
  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  @Column(name = "evid")
  public long getEventId() {
    return eventId;
  }

  public void setEventId(long eventId) {
    this.eventId = eventId;
  }

  @Column(name = "magtype")
  public String getMagnitudeType() {
    return magnitudeType;
  }

  public void setMagnitudeType(String magnitudeType) {
    this.magnitudeType = magnitudeType;
  }

  @Column(name = "nsta")
  public int getNumberOfStations() {
    return numberOfStations;
  }

  public void setNumberOfStations(int numberOfStations) {
    this.numberOfStations = numberOfStations;
  }

  @Column(name = "magnitude")
  public double getMagnitude() {
    return magnitude;
  }

  public void setMagnitude(double magnitude) {
    this.magnitude = magnitude;
  }

  @Column(name = "uncertainty")
  public double getMagnitudeUncertainty() {
    return magnitudeUncertainty;
  }

  public void setMagnitudeUncertainty(double magnitudeUncertainty) {
    this.magnitudeUncertainty = magnitudeUncertainty;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "commid")
  public long getCommentId() {
    return commentId;
  }

  public void setCommentId(long commentId) {
    this.commentId = commentId;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  public static class Builder {

    private long magnitudeId;
    private String network;
    private long originId;
    private long eventId;
    private String magnitudeType;
    private int numberOfStations;
    private double magnitude;
    private double magnitudeUncertainty;
    private String author;
    private long commentId;
    private Instant loadDate;

    public static Builder initializeFromInstance(NetMagDao netMagDao) {

      return new NetMagDao.Builder()
        .withMagnitudeId(netMagDao.magnitudeId)
        .withNetwork(netMagDao.network)
        .withOriginId(netMagDao.originId)
        .withEventId(netMagDao.eventId)
        .withMagnitudeType(netMagDao.magnitudeType)
        .withNumberOfStations(netMagDao.numberOfStations)
        .withMagnitude(netMagDao.magnitude)
        .withMagnitudeUncertainty(netMagDao.magnitudeUncertainty)
        .withAuthor(netMagDao.author)
        .withCommentId(netMagDao.commentId)
        .withLoadDate(netMagDao.loadDate);
    }

    public Builder withMagnitudeId(long magnitudeId) {
      this.magnitudeId = magnitudeId;
      return this;
    }

    public Builder withNetwork(String network) {
      this.network = network;
      return this;
    }

    public Builder withOriginId(long originId) {
      this.originId = originId;
      return this;
    }

    public Builder withEventId(long eventId) {
      this.eventId = eventId;
      return this;
    }

    public Builder withMagnitudeType(String magnitudeType) {
      this.magnitudeType = magnitudeType;
      return this;
    }

    public Builder withNumberOfStations(int numberOfStations) {
      this.numberOfStations = numberOfStations;
      return this;
    }

    public Builder withMagnitude(double magnitude) {
      this.magnitude = magnitude;
      return this;
    }

    public Builder withMagnitudeUncertainty(double magnitudeUncertainty) {
      this.magnitudeUncertainty = magnitudeUncertainty;
      return this;
    }

    public Builder withAuthor(String author) {
      this.author = author;
      return this;
    }

    public Builder withCommentId(long commentId) {
      this.commentId = commentId;
      return this;
    }

    public Builder withLoadDate(Instant loadDate) {
      this.loadDate = loadDate;
      return this;
    }

    public NetMagDao build() {

      // -1 indicates NA value
      if (-1 != magnitudeId) {
        checkArgument(0 <= magnitudeId, "Magnitude Id is " + magnitudeId +
          ".  It must be non-negative.");
      }

      checkNotNull(network, "Network identifier is null.");
      checkArgument(!network.isBlank(), "Network identifier is blank.");
      // "-" indicates NA value
      if (!"-".equals(network)) {
        checkArgument(network.length() <= 8, "Network identifier is " + network +
          ".  Length of string must be no greater than 8.");
      }

      // NA not allowed
      checkArgument(0 < originId, "The value of Origin Id is " + originId +
        DaoHelperUtility.createGreaterThanString(0));

      // -1 indicates NA value (DBDD mistakenly says 1 is NA)
      if (-1 != eventId) {
        checkArgument(0 < eventId, "Event Id is " + eventId +
          DaoHelperUtility.createGreaterThanString(0));
      }

      checkNotNull(magnitudeType, "Magnitude type is null.");
      checkArgument(!magnitudeType.isBlank(), "Magnitude type is blank.");
      // NA not allowed
      checkArgument(magnitudeType.length() <= 6, "Magnitude type is " + magnitudeType +
        ".  Length of string must be no greater than 6.");

      // -1 indicated NA value
      if (-1 != numberOfStations) {
        checkArgument(0 <= numberOfStations, "Number of stations is " + numberOfStations +
          ".  It must be non-negative.");
      }

      // -999.0 indicates NA value
      if (magnitude != -999.0) {
        checkArgument((-9.99 < magnitude) && (magnitude < 50.0), "Magnitude is " + magnitude +
          DaoHelperUtility.createRangeStringDouble(-9.99, 50, '(', ')'));
      }

      // -1.0 indicates NA value
      if (magnitudeUncertainty != -1.0) {
        checkArgument(0.0 < magnitudeUncertainty,
          "Magnitude uncertainty is " + magnitudeUncertainty +
            DaoHelperUtility.createGreaterThanString(0));
      }

      if (author == null) {
        author = "-";
      }
      checkArgument(!author.isBlank(), "Author is blank.");
      // "-" indicates NA value
      if (!"-".equals(author)) {
        checkArgument(author.length() <= 15, "Author is " + author +
          DaoHelperUtility.createCharLengthString(15));
      }

      // -1 indicates NA value
      if (commentId != -1) {
        checkArgument(0 < commentId, "Comment Id is " + commentId +
          DaoHelperUtility.createGreaterThanString(0));
      }

      checkNotNull(loadDate, "Load date is null.");

      return new NetMagDao(this);
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NetMagDao)) {
      return false;
    }
    var netMagDao = (NetMagDao) o;
    return magnitudeId == netMagDao.magnitudeId && originId == netMagDao.originId
      && eventId == netMagDao.eventId && numberOfStations == netMagDao.numberOfStations
      && Double.compare(netMagDao.magnitude, magnitude) == 0
      && Double.compare(netMagDao.magnitudeUncertainty, magnitudeUncertainty) == 0
      && commentId == netMagDao.commentId && network.equals(netMagDao.network)
      && magnitudeType.equals(netMagDao.magnitudeType) && author.equals(netMagDao.author)
      && loadDate.equals(netMagDao.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(magnitudeId, network, originId, eventId, magnitudeType,
      numberOfStations, magnitude, magnitudeUncertainty, author, commentId, loadDate);
  }

  @Override
  public String toString() {
    return "NetMagDao{" +
      "magnitudeId=" + magnitudeId +
      ", network='" + network + '\'' +
      ", originId=" + originId +
      ", eventId=" + eventId +
      ", magnitudeType='" + magnitudeType + '\'' +
      ", numberOfStations=" + numberOfStations +
      ", magnitude=" + magnitude +
      ", magnitudeUncertainty=" + magnitudeUncertainty +
      ", author='" + author + '\'' +
      ", commentId=" + commentId +
      ", loadDate=" + loadDate +
      '}';
  }
}
