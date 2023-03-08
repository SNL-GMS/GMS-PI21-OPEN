package gms.shared.event.dao;

import gms.shared.signaldetection.dao.css.converter.DefiningFlagConverter;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a Station Magnitude record in the `stamag` legacy table.
 */
@Entity
@Table(name = "stamag")
public class StaMagDao {

  private MagnitudeIdAmplitudeIdStationNameKey magnitudeIdAmplitudeIdStationNameKey;
  private long arrivalId;
  private long originId;
  private long eventId;
  private String phaseType;
  private double delta;  // station-to-station distance, units=degrees
  private String magnitudeType;  // ms, mb, etc.
  private double magnitude;  // units=magnitude
  private double magnitudeUncertainty;
  private double magnitudeResidual; // units=magnitude
  private DefiningFlag magnitudeDefining;  // d, D, n, N, x, or X
  private String magnitudeModel;
  private String author;
  private long commentId;
  private Instant loadDate;


  public StaMagDao() {
  }

  private StaMagDao(Builder builder) {
    this.magnitudeIdAmplitudeIdStationNameKey = builder.magnitudeIdAmplitudeIdStationNameKey;
    this.arrivalId = builder.arrivalId;
    this.originId = builder.originId;
    this.eventId = builder.eventId;
    this.phaseType = builder.phaseType;
    this.delta = builder.delta;
    this.magnitudeType = builder.magnitudeType;
    this.magnitude = builder.magnitude;
    this.magnitudeUncertainty = builder.magnitudeUncertainty;
    this.magnitudeResidual = builder.magnitudeResidual;
    this.magnitudeDefining = builder.magnitudeDefining;
    this.magnitudeModel = builder.magnitudeModel;
    this.author = builder.author;
    this.commentId = builder.commentId;
    this.loadDate = builder.loadDate;
  }

  @EmbeddedId
  public MagnitudeIdAmplitudeIdStationNameKey getMagAmpStaKey() {
    return magnitudeIdAmplitudeIdStationNameKey;
  }

  public void setMagAmpStaKey(
    MagnitudeIdAmplitudeIdStationNameKey magnitudeIdAmplitudeIdStationNameKey) {
    checkNotNull(magnitudeIdAmplitudeIdStationNameKey,
      "MagnitudeIdAmplitudeIdStationNameKey is null.");
    this.magnitudeIdAmplitudeIdStationNameKey = magnitudeIdAmplitudeIdStationNameKey;
  }

  @Transient
  public long getMagnitudeId() {
    return getMagAmpStaKey().getMagnitudeId();
  }

  @Transient
  public long getAmplitudeId() {
    return getMagAmpStaKey().getAmplitudeId();
  }

  @Transient
  public String getStationName() {
    return getMagAmpStaKey().getStationName();
  }

  @Column(name = "arid")
  public long getArrivalId() {
    return this.arrivalId;
  }

  public void setArrivalId(long arrivalId) {
    this.arrivalId = arrivalId;
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

  @Column(name = "phase")
  public String getPhaseType() {
    return this.phaseType;
  }

  public void setPhaseType(String phaseType) {
    this.phaseType = phaseType;
  }

  @Column(name = "delta")
  public double getDelta() {
    return delta;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }

  @Column(name = "magtype")
  public String getMagnitudeType() {
    return magnitudeType;
  }

  public void setMagnitudeType(String magnitudeType) {
    this.magnitudeType = magnitudeType;
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

  @Column(name = "magres")
  public double getMagnitudeResidual() {
    return magnitudeResidual;
  }

  public void setMagnitudeResidual(double magnitudeResidual) {
    this.magnitudeResidual = magnitudeResidual;
  }

  @Column(name = "magdef", nullable = false)
  @Convert(converter = DefiningFlagConverter.class)
  public DefiningFlag getMagnitudeDefining() {
    return magnitudeDefining;
  }

  public void setMagnitudeDefining(DefiningFlag magnitudeDefining) {
    this.magnitudeDefining = magnitudeDefining;
  }

  @Column(name = "mmodel")
  public String getMagnitudeModel() {
    return magnitudeModel;
  }

  public void setMagnitudeModel(String magnitudeModel) {
    this.magnitudeModel = magnitudeModel;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return this.author;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StaMagDao)) {
      return false;
    }
    var staMagDao = (StaMagDao) o;

    return arrivalId == staMagDao.arrivalId && originId == staMagDao.originId
      && eventId == staMagDao.eventId && Double.compare(staMagDao.delta, delta) == 0
      && Double.compare(staMagDao.magnitude, magnitude) == 0
      && Double.compare(staMagDao.magnitudeUncertainty, magnitudeUncertainty) == 0
      && Double.compare(staMagDao.magnitudeResidual, magnitudeResidual) == 0
      && commentId == staMagDao.commentId && Objects.equals(
      magnitudeIdAmplitudeIdStationNameKey, staMagDao.magnitudeIdAmplitudeIdStationNameKey)
      && Objects.equals(phaseType, staMagDao.phaseType) && Objects.equals(
      magnitudeType, staMagDao.magnitudeType) && Objects.equals(magnitudeDefining,
      staMagDao.magnitudeDefining) && Objects.equals(magnitudeModel,
      staMagDao.magnitudeModel) && Objects.equals(author, staMagDao.author)
      && Objects.equals(loadDate, staMagDao.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(magnitudeIdAmplitudeIdStationNameKey, arrivalId, originId, eventId,
      phaseType,
      delta, magnitudeType, magnitude, magnitudeUncertainty, magnitudeResidual, magnitudeDefining,
      magnitudeModel, author, commentId, loadDate);
  }

  @Override
  public String toString() {
    return "StaMagDao{" +
      "magnitudeIdAmplitudeIdStationNameKey=" + magnitudeIdAmplitudeIdStationNameKey +
      ", arrivalId=" + arrivalId +
      ", originId=" + originId +
      ", eventId=" + eventId +
      ", phaseType='" + phaseType + '\'' +
      ", delta=" + delta +
      ", magnitudeType='" + magnitudeType + '\'' +
      ", magnitude=" + magnitude +
      ", magnitudeUncertainty=" + magnitudeUncertainty +
      ", magnitudeResidual=" + magnitudeResidual +
      ", magnitudeDefining=" + magnitudeDefining +
      ", magnitudeModel='" + magnitudeModel + '\'' +
      ", author='" + author + '\'' +
      ", commentId=" + commentId +
      ", loadDate=" + loadDate +
      '}';
  }

  public static class Builder {

    private MagnitudeIdAmplitudeIdStationNameKey magnitudeIdAmplitudeIdStationNameKey;
    private long arrivalId;
    private long originId;
    private long eventId;
    private String phaseType;
    private double delta;
    private String magnitudeType;
    private double magnitude;
    private double magnitudeUncertainty;
    private double magnitudeResidual;
    private DefiningFlag magnitudeDefining;
    private String magnitudeModel;
    private String author;
    private long commentId;
    private Instant loadDate;

    public static Builder initializeFromInstance(StaMagDao staMagDao) {

      return new StaMagDao.Builder()
        .withMagnitudeIdAmplitudeIdStationNameKey(
          new MagnitudeIdAmplitudeIdStationNameKey.Builder()
            .withMagnitudeId(staMagDao.getMagnitudeId())
            .withAmplitudeId(staMagDao.getAmplitudeId())
            .withStationName(staMagDao.getStationName())
            .build()
        )
        .withArrivalId(staMagDao.arrivalId)
        .withOriginId(staMagDao.originId)
        .withEventId(staMagDao.eventId)
        .withPhaseType(staMagDao.phaseType)
        .withDelta(staMagDao.delta)
        .withMagnitudeType(staMagDao.magnitudeType)
        .withMagnitude(staMagDao.magnitude)
        .withMagnitudeUncertainty(staMagDao.magnitudeUncertainty)
        .withMagnitudeResidual(staMagDao.magnitudeResidual)
        .withMagnitudeDefining(staMagDao.magnitudeDefining)
        .withMagnitudeModel(staMagDao.magnitudeModel)
        .withAuthor(staMagDao.author)
        .withCommentId(staMagDao.commentId)
        .withLoadDate(staMagDao.loadDate);
    }

    public Builder withMagnitudeIdAmplitudeIdStationNameKey(
      MagnitudeIdAmplitudeIdStationNameKey magnitudeIdAmplitudeIdStationNameKey) {
      this.magnitudeIdAmplitudeIdStationNameKey = magnitudeIdAmplitudeIdStationNameKey;
      return this;
    }

    public Builder withArrivalId(long arrivalId) {
      this.arrivalId = arrivalId;
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

    public Builder withPhaseType(String phaseType) {
      this.phaseType = phaseType;
      return this;
    }

    public Builder withDelta(double delta) {
      this.delta = delta;
      return this;
    }

    public Builder withMagnitudeType(String magnitudeType) {
      this.magnitudeType = magnitudeType;
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

    public Builder withMagnitudeResidual(double magnitudeResidual) {
      this.magnitudeResidual = magnitudeResidual;
      return this;
    }

    public Builder withMagnitudeDefining(DefiningFlag magnitudeDefining) {
      this.magnitudeDefining = magnitudeDefining;
      return this;
    }

    public Builder withMagnitudeModel(String magnitudeModel) {
      this.magnitudeModel = magnitudeModel;
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

    public StaMagDao build() {

      checkNotNull(magnitudeIdAmplitudeIdStationNameKey,
        "MagnitudeIdAmplitudeIdStationNameKey is null");

      // -1 indicates NA value
      if (arrivalId != -1) {
        checkArgument(0 < arrivalId, "Arrival Id is " + arrivalId +
          DaoHelperUtility.createGreaterThanString(0));
      }

      // NA not allowed
      checkArgument(0 < originId, "Origin Id is " + originId +
        DaoHelperUtility.createGreaterThanString(0));

      // -1 indicates NA value (DBDD mistakenly says 1 is NA)
      if (-1 != eventId) {
        checkArgument(0 < eventId, "Event Id is " + eventId +
          ".  It must be greater than 0.");
      }

      checkNotNull(phaseType, "Phase type is null.");
      checkArgument(!phaseType.isBlank(), "Phase type is blank.");
      // NA not allowed
      checkArgument(phaseType.length() <= 8, "Phase type is " + phaseType +
        DaoHelperUtility.createCharLengthString(8));

      // -1.0 indicates NA value
      if (delta != -1.0) {
        checkArgument(0.0 <= delta, "Source-receiver distance, delta is " + delta +
          ". It must be non-negative.");
      }

      checkNotNull(magnitudeType, "Magnitude type is null.");
      checkArgument(!magnitudeType.isBlank(), "Magnitude type is blank.");
      // NA is not allowed
      checkArgument(magnitudeType.length() <= 6, "Magnitude type is " + magnitudeType +
        DaoHelperUtility.createCharLengthString(6));

      // -999.0 indicates NA value
      if (magnitude != -999.0) {
        checkArgument((-9.99 < magnitude) && (magnitude < 50.0), "magnitude is " + magnitude +
          DaoHelperUtility.createRangeStringDouble(-9.99, 50, '(', ')'));
      }

      // -1.0 indicates NA value
      if (magnitudeUncertainty != -1.0) {
        checkArgument(0.0 < magnitudeUncertainty,
          "Magnitude uncertainty is " + magnitudeUncertainty +
            ". It must be greater than zero.");
      }

      // -999.0 indicates NA value
      if (magnitudeResidual != -999.0) {
        checkArgument((-50.0 < magnitudeResidual) && (magnitudeResidual < 50.0),
          "Magnitude Residual is " + magnitudeResidual +
            DaoHelperUtility.createRangeStringDouble(-50, 50, '(', ')'));
      }

      checkNotNull(magnitudeModel, "Magnitude model is null.");
      checkArgument(!magnitudeModel.isBlank(), "Magnitude model is blank.");
      // "-" indicates NA value
      if (!"-".equals(magnitudeModel)) {
        checkArgument(magnitudeModel.length() <= 15, "Magnitude model is " + magnitudeModel +
          DaoHelperUtility.createCharLengthString(15));
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
          ".  It must be greater than 0.");
      }

      checkNotNull(loadDate, "Load date is null.");

      return new StaMagDao(this);
    }
  }
}
