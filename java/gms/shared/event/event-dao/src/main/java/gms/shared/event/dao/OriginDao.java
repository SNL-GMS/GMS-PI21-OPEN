package gms.shared.event.dao;

import gms.shared.event.coi.type.DepthMethod;
import gms.shared.event.coi.type.converter.DepthMethodConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an origin record in the `origin` legacy table.
 */
@Entity
@NamedQuery(
  name = "origin.findByTime",
  query = "Select ori from OriginDao ori, OrigerrDao err where ori.originId = err.originId " +
    "and ((ori.latLonDepthTimeKey.time + err.originTimeError) >= :startTime) " +
    "and ((ori.latLonDepthTimeKey.time - err.originTimeError) <= :endTime)")
@Table(name = "origin")
public class OriginDao {

  private LatLonDepthTimeKey latLonDepthTimeKey;
  private long originId;
  private long eventId;
  private long julianDate;
  private long numAssociatedArrivals;
  private long numTimeDefiningPhases;
  private long numDepthPhases;
  private long geographicRegionNumber;
  private long seismicRegionNumber;
  private String eventType;
  private double estimatedDepth;
  private DepthMethod depthMethod;
  private double bodyWaveMag;
  private long bodyWaveMagId;
  private double surfaceWaveMag;
  private long surfaceWaveMagId;
  private double localMag;
  private long localMagId;
  private String locationAlgorithm;
  private String author;
  private long commentId;
  private Instant loadDate;

  public OriginDao() {
  }

  private OriginDao(Builder builder) {
    this.latLonDepthTimeKey = builder.latLonDepthTimeKey;
    this.originId = builder.originId;
    this.eventId = builder.eventId;
    this.julianDate = builder.julianDate;
    this.numAssociatedArrivals = builder.numAssociatedArrivals;
    this.numTimeDefiningPhases = builder.numTimeDefiningPhases;
    this.numDepthPhases = builder.numDepthPhases;
    this.geographicRegionNumber = builder.geographicRegionNumber;
    this.seismicRegionNumber = builder.seismicRegionNumber;
    this.eventType = builder.eventType;
    this.estimatedDepth = builder.estimatedDepth;
    this.depthMethod = builder.depthMethod;
    this.bodyWaveMag = builder.bodyWaveMag;
    this.bodyWaveMagId = builder.bodyWaveMagId;
    this.surfaceWaveMag = builder.surfaceWaveMag;
    this.surfaceWaveMagId = builder.surfaceWaveMagId;
    this.localMag = builder.localMag;
    this.localMagId = builder.localMagId;
    this.locationAlgorithm = builder.locationAlgorithm;
    this.author = builder.author;
    this.commentId = builder.commentId;
    this.loadDate = builder.loadDate;
  }

  @EmbeddedId
  public LatLonDepthTimeKey getLatLonDepthTimeKey() {
    return latLonDepthTimeKey;
  }

  public void setLatLonDepthTimeKey(LatLonDepthTimeKey latLonDepthTimeKey) {
    this.latLonDepthTimeKey = latLonDepthTimeKey;
  }

  @Transient
  public double getLatitude() {
    return getLatLonDepthTimeKey().getLatitude();
  }

  @Transient
  public double getLongitude() {
    return getLatLonDepthTimeKey().getLongitude();
  }

  @Transient
  public double getDepth() {
    return getLatLonDepthTimeKey().getDepth();
  }

  @Transient
  public double getEpoch() {
    return getLatLonDepthTimeKey().getTime();
  }

  @Column(name = "orId")
  public long getOriginId() {
    return originId;
  }

  public void setOriginId(long originId) {
    this.originId = originId;
  }

  @Column(name = "evId")
  public long getEventId() {
    return eventId;
  }

  public void setEventId(long eventId) {
    this.eventId = eventId;
  }

  @Column(name = "jdate")
  public long getJulianDate() {
    return julianDate;
  }

  public void setJulianDate(long julianDate) {
    this.julianDate = julianDate;
  }

  @Column(name = "nass")
  public long getNumAssociatedArrivals() {
    return numAssociatedArrivals;
  }

  public void setNumAssociatedArrivals(long numAssociatedArrivals) {
    this.numAssociatedArrivals = numAssociatedArrivals;
  }

  @Column(name = "ndef")
  public long getNumTimeDefiningPhases() {
    return numTimeDefiningPhases;
  }

  public void setNumTimeDefiningPhases(long numTimeDefiningPhases) {
    this.numTimeDefiningPhases = numTimeDefiningPhases;
  }

  @Column(name = "ndp")
  public long getNumDepthPhases() {
    return numDepthPhases;
  }

  public void setNumDepthPhases(long numDepthPhases) {
    this.numDepthPhases = numDepthPhases;
  }

  @Column(name = "grn")
  public long getGeographicRegionNumber() {
    return geographicRegionNumber;
  }

  public void setGeographicRegionNumber(long geographicRegionNumber) {
    this.geographicRegionNumber = geographicRegionNumber;
  }

  @Column(name = "srn")
  public long getSeismicRegionNumber() {
    return seismicRegionNumber;
  }

  public void setSeismicRegionNumber(long seismicRegionNumber) {
    this.seismicRegionNumber = seismicRegionNumber;
  }

  @Column(name = "eType")
  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  @Column(name = "depdp")
  public double getEstimatedDepth() {
    return estimatedDepth;
  }

  public void setEstimatedDepth(double estimatedDepth) {
    this.estimatedDepth = estimatedDepth;
  }

  @Column(name = "dType", nullable = false)
  @Convert(converter = DepthMethodConverter.class)
  public DepthMethod getDepthMethod() {
    return depthMethod;
  }

  public void setDepthMethod(DepthMethod depthMethod) {
    this.depthMethod = depthMethod;
  }

  @Column(name = "mb")
  public double getBodyWaveMag() {
    return bodyWaveMag;
  }

  public void setBodyWaveMag(double bodyWaveMag) {
    this.bodyWaveMag = bodyWaveMag;
  }

  @Column(name = "mbid")
  public long getBodyWaveMagId() {
    return bodyWaveMagId;
  }

  public void setBodyWaveMagId(long bodyWaveMagId) {
    this.bodyWaveMagId = bodyWaveMagId;
  }

  @Column(name = "ms")
  public double getSurfaceWaveMag() {
    return surfaceWaveMag;
  }

  public void setSurfaceWaveMag(double surfaceWaveMag) {
    this.surfaceWaveMag = surfaceWaveMag;
  }

  @Column(name = "msId")
  public long getSurfaceWaveMagId() {
    return surfaceWaveMagId;
  }

  public void setSurfaceWaveMagId(long surfaceWaveMagId) {
    this.surfaceWaveMagId = surfaceWaveMagId;
  }

  @Column(name = "ml")
  public double getLocalMag() {
    return localMag;
  }

  public void setLocalMag(double localMag) {
    this.localMag = localMag;
  }

  @Column(name = "mlId")
  public long getLocalMagId() {
    return localMagId;
  }

  public void setLocalMagId(long localMagId) {
    this.localMagId = localMagId;
  }

  @Column(name = "algorithm")
  public String getLocationAlgorithm() {
    return locationAlgorithm;
  }

  public void setLocationAlgorithm(String locationAlgorithm) {
    this.locationAlgorithm = locationAlgorithm;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "commId")
  public long getCommentId() {
    return commentId;
  }

  public void setCommentId(long commentId) {
    this.commentId = commentId;
  }

  @Column(name = "ldDate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  public static class Builder {

    private LatLonDepthTimeKey latLonDepthTimeKey;
    private long originId;
    private long eventId;
    private long julianDate;
    private long numAssociatedArrivals;
    private long numTimeDefiningPhases;
    private long numDepthPhases;
    private long geographicRegionNumber;
    private long seismicRegionNumber;
    private String eventType;
    private double estimatedDepth;
    private DepthMethod depthMethod;
    private double bodyWaveMag;
    private long bodyWaveMagId;
    private double surfaceWaveMag;
    private long surfaceWaveMagId;
    private double localMag;
    private long localMagId;
    private String locationAlgorithm;
    private String author;
    private long commentId;
    private Instant loadDate;

    public static OriginDao.Builder initializeFromInstance(OriginDao originDao) {
      return new OriginDao.Builder()
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(originDao.getLatLonDepthTimeKey().getLatitude())
            .withLongitude(originDao.getLatLonDepthTimeKey().getLongitude())
            .withDepth(originDao.getLatLonDepthTimeKey().getDepth())
            .withTime(originDao.getLatLonDepthTimeKey().getTime())
            .build()
        )
        .withOriginId(originDao.getOriginId())
        .withEventId(originDao.getEventId())
        .withJulianDate(originDao.getJulianDate())
        .withNumAssociatedArrivals(originDao.getNumAssociatedArrivals())
        .withNumTimeDefiningPhases(originDao.getNumTimeDefiningPhases())
        .withNumDepthPhases(originDao.getNumDepthPhases())
        .withGeographicRegionNumber(originDao.getGeographicRegionNumber())
        .withSeismicRegionNumber(originDao.getSeismicRegionNumber())
        .withEventType(originDao.getEventType())
        .withEstimatedDepth(originDao.getEstimatedDepth())
        .withDepthMethod(originDao.getDepthMethod())
        .withBodyWaveMag(originDao.getBodyWaveMag())
        .withBodyWaveMagId(originDao.getBodyWaveMagId())
        .withSurfaceWaveMag(originDao.getSurfaceWaveMag())
        .withSurfaceWaveMagId(originDao.getSurfaceWaveMagId())
        .withLocalMag(originDao.getLocalMag())
        .withLocalMagId(originDao.getLocalMagId())
        .withLocationAlgorithm(originDao.getLocationAlgorithm())
        .withAuthor(originDao.getAuthor())
        .withCommentId(originDao.getCommentId())
        .withLoadDate(originDao.getLoadDate());
    }

    public Builder withLatLonDepthTimeKey(LatLonDepthTimeKey latLonDepthTimeKey) {
      this.latLonDepthTimeKey = latLonDepthTimeKey;
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

    public Builder withJulianDate(long julianDate) {
      // -1 indicates NA value
      this.julianDate = julianDate;
      return this;
    }

    public Builder withNumAssociatedArrivals(long numAssociatedArrivals) {
      this.numAssociatedArrivals = numAssociatedArrivals;
      return this;
    }

    public Builder withNumTimeDefiningPhases(long numTimeDefiningPhases) {
      // Number of time-defining phases must be in the range (0, numAssociatedArrivals].
      // Therefore, we must wait until build() is called to perform validation checks.
      this.numTimeDefiningPhases = numTimeDefiningPhases;
      return this;
    }

    public Builder withNumDepthPhases(long numDepthPhases) {
      this.numDepthPhases = numDepthPhases;
      return this;
    }

    public Builder withGeographicRegionNumber(long geographicRegionNumber) {
      this.geographicRegionNumber = geographicRegionNumber;
      return this;
    }

    public Builder withSeismicRegionNumber(long seismicRegionNumber) {
      this.seismicRegionNumber = seismicRegionNumber;
      return this;
    }

    public Builder withEventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder withEstimatedDepth(double estimatedDepth) {
      this.estimatedDepth = estimatedDepth;
      return this;
    }

    public Builder withDepthMethod(DepthMethod depthMethod) {
      this.depthMethod = depthMethod;
      return this;
    }

    public Builder withBodyWaveMag(double bodyWaveMag) {
      this.bodyWaveMag = bodyWaveMag;
      return this;
    }

    public Builder withBodyWaveMagId(long bodyWaveMagId) {
      this.bodyWaveMagId = bodyWaveMagId;
      return this;
    }

    public Builder withSurfaceWaveMag(double surfaceWaveMag) {
      this.surfaceWaveMag = surfaceWaveMag;
      return this;
    }

    public Builder withSurfaceWaveMagId(long surfaceWaveMagId) {
      this.surfaceWaveMagId = surfaceWaveMagId;
      return this;
    }

    public Builder withLocalMag(double localMag) {
      this.localMag = localMag;
      return this;
    }

    public Builder withLocalMagId(long localMagId) {
      this.localMagId = localMagId;
      return this;
    }

    public Builder withLocationAlgorithm(String locationAlgorithm) {
      this.locationAlgorithm = locationAlgorithm;
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

    public OriginDao build() {

      if (author == null) {
        author = "-";
      }
      checkArgument(!author.isBlank(), "Author is blank.");
      // "-" indicates NA value
      if (!"-".equals(author)) {
        checkArgument(author.length() <= 15, "Author is " + author +
          DaoHelperUtility.createCharLengthString(15));
      }

      // -999.0 indicates NA value
      DaoHelperUtility.checkRange(
        bodyWaveMag, -999.0, -9.99, 50.0, true, true,
        "Body wave magnitude is " + bodyWaveMag + DaoHelperUtility.createRangeStringDouble(
          -9.99, 50.0, '(', ')')
      );

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        bodyWaveMagId, -1L, 0L, Long.MAX_VALUE, true, false,
        "Body wave magnitude ID is " + bodyWaveMagId + DaoHelperUtility.createGreaterThanString(0)
      );

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        commentId, -1L, 0L, Long.MAX_VALUE, true, false,
        "Comment Id is " + commentId + DaoHelperUtility.createGreaterThanString(0)
      );

      checkNotNull(depthMethod, "Depth determination flag is null.");

      // -999.0 indicates NA value
      DaoHelperUtility.checkRange(
        estimatedDepth, -999.0, 0.0, 1000.0, false, true,
        "Depth as estimated from depth phases is " + estimatedDepth +
          DaoHelperUtility.createRangeStringDouble(0.0, 1000.0, '[', ')')
      );

      // -1 indicates NA value (DBDD mistakenly says 1 is NA)
      DaoHelperUtility.checkRange(
        eventId, -1L, 0L, Long.MAX_VALUE, true, false,
        "Event Id is " + eventId + DaoHelperUtility.createGreaterThanString(0)
      );

      checkNotNull(eventType, "Event type is null.");
      checkArgument(!eventType.isBlank(), "Event type is blank.");
      // "-" indicates NA value
      if (!"-".equals(eventType)) {
        checkArgument(eventType.equals(eventType.toLowerCase(Locale.ENGLISH)) && eventType.length() <= 7,
          "Event type is " + eventType +
            ".  It must be a lower case string of at most 7 characters.");
      }

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        geographicRegionNumber, -1L, 1L, 729L, false, false,
        "Geographic region number is " + geographicRegionNumber +
          DaoHelperUtility.createRangeStringInt(1, 729, '[', ']')
      );

      checkNotNull(loadDate, "Load date is null.");

      checkNotNull(latLonDepthTimeKey, "LatLonDepthTimeKey is null.");

      // -999.0 indicates NA value
      DaoHelperUtility.checkRange(
        localMag, -999.0, -9.99, 50.0, true, true,
        "Local magnitude is " + localMag +
          DaoHelperUtility.createRangeStringDouble(-9.99, 50.0, '(', ')')
      );

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        localMagId, -1L, 0L, Long.MAX_VALUE, true, false,
        "Local magnitude ID is " + localMagId +
          DaoHelperUtility.createGreaterThanString(0)
      );

      checkNotNull(locationAlgorithm, "Location algorithm is null.");
      checkArgument(!locationAlgorithm.isBlank(), "Location algorithm is blank.");
      // "-" indicates NA value
      if (!"-".equals(locationAlgorithm)) {
        checkArgument(locationAlgorithm.length() <= 15,
          "Location algorithm is " + locationAlgorithm +
            DaoHelperUtility.createCharLengthString(15));
      }

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        numAssociatedArrivals, -1L, 0L, Long.MAX_VALUE, true, false,
        "Number of associated arrivals is " + numAssociatedArrivals +
          DaoHelperUtility.createGreaterThanString(0)
      );

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        numDepthPhases, -1L, 0L, Long.MAX_VALUE, false, false,
        "Number of depth phases is " + numDepthPhases + ".  It must be non-negative."
      );

      // NA not allowed
      checkArgument(0 < originId, "The value of Origin Id is " + originId +
        DaoHelperUtility.createGreaterThanString(0));

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        seismicRegionNumber, -1L, 1L, 50L, false, false,
        "Seismic region number is " + seismicRegionNumber +
          DaoHelperUtility.createRangeStringInt(1, 50, '[', ']')
      );

      // -999.0 indicates NA value
      DaoHelperUtility.checkRange(
        surfaceWaveMag, -999.0, -9.99, 50.0, true, true,
        "Surface wave magnitude is " + surfaceWaveMag +
          DaoHelperUtility.createRangeStringDouble(-9.99, 50.0, '(', ')')
      );

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        surfaceWaveMagId, -1L, 0L, Long.MAX_VALUE, true, false,
        "Surface wave magnitude ID is " + surfaceWaveMagId +
          DaoHelperUtility.createGreaterThanString(0)
      );

      // -1 indicates NA value
      DaoHelperUtility.checkRange(
        numTimeDefiningPhases, -1L, 0L, numAssociatedArrivals, true, false,
        "Number of time-defining phases is " + numTimeDefiningPhases +
          DaoHelperUtility.createRangeStringInt(0, numAssociatedArrivals, '(', ']') +
          "since the number of associated arrivals is " + numAssociatedArrivals + "."
      );

      return new OriginDao(this);
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var originDao = (OriginDao) o;
    return originId == originDao.originId && eventId == originDao.eventId
      && julianDate == originDao.julianDate
      && numAssociatedArrivals == originDao.numAssociatedArrivals
      && numTimeDefiningPhases == originDao.numTimeDefiningPhases
      && numDepthPhases == originDao.numDepthPhases
      && geographicRegionNumber == originDao.geographicRegionNumber
      && seismicRegionNumber == originDao.seismicRegionNumber
      && Double.compare(originDao.estimatedDepth, estimatedDepth) == 0
      && Double.compare(originDao.bodyWaveMag, bodyWaveMag) == 0
      && bodyWaveMagId == originDao.bodyWaveMagId
      && Double.compare(originDao.surfaceWaveMag, surfaceWaveMag) == 0
      && surfaceWaveMagId == originDao.surfaceWaveMagId
      && Double.compare(originDao.localMag, localMag) == 0 && localMagId == originDao.localMagId
      && commentId == originDao.commentId
      && Objects.equals(latLonDepthTimeKey, originDao.latLonDepthTimeKey) && Objects.equals(
      eventType, originDao.eventType)
      && Objects.equals(depthMethod, originDao.depthMethod) && Objects.equals(locationAlgorithm,
      originDao.locationAlgorithm)
      && Objects.equals(author, originDao.author) && Objects.equals(loadDate, originDao.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latLonDepthTimeKey, originId, eventId, julianDate, numAssociatedArrivals,
      numTimeDefiningPhases,
      numDepthPhases, geographicRegionNumber, seismicRegionNumber, eventType, estimatedDepth,
      depthMethod, bodyWaveMag,
      bodyWaveMagId, surfaceWaveMag, surfaceWaveMagId, localMag, localMagId, locationAlgorithm,
      author, commentId, loadDate);
  }

  @Override
  public String toString() {
    return "OriginDao{" +
      "latLonDepthTimeKey=" + latLonDepthTimeKey +
      ", originId=" + originId +
      ", eventId=" + eventId +
      ", julianDate=" + julianDate +
      ", numAssociatedArrivals=" + numAssociatedArrivals +
      ", numTimeDefiningPhases=" + numTimeDefiningPhases +
      ", numDepthPhases=" + numDepthPhases +
      ", geographicRegionNumber=" + geographicRegionNumber +
      ", seismicRegionNumber=" + seismicRegionNumber +
      ", eventType='" + eventType + '\'' +
      ", estimatedDepth=" + estimatedDepth +
      ", depthMethod='" + depthMethod + '\'' +
      ", bodyWaveMag=" + bodyWaveMag +
      ", bodyWaveMagId=" + bodyWaveMagId +
      ", surfaceWaveMag=" + surfaceWaveMag +
      ", surfaceWaveMagId=" + surfaceWaveMagId +
      ", localMag=" + localMag +
      ", localMagId=" + localMagId +
      ", locationAlgorithm='" + locationAlgorithm + '\'' +
      ", author='" + author + '\'' +
      ", commentId=" + commentId +
      ", loadDate=" + loadDate +
      '}';
  }
}
