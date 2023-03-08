package gms.shared.signaldetection.dao.css;

import gms.shared.signaldetection.dao.css.converter.DefiningFlagConverter;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "assoc")
public class AssocDao {

  private AridOridKey id;
  private String stationCode;
  private String phase;
  private double belief;
  private double delta;
  private double stationToEventAzimuth;
  private double eventToStationAzimuth;
  private double timeResidual;
  private DefiningFlag timeDefining;
  private double azimuthResidual;
  private DefiningFlag azimuthDefining;
  private double slownessResidual;
  private DefiningFlag slownessDefining;
  private double emergenceAngleResidual;
  private double locationWeight;
  private String velocityModel;
  private long commentId;
  private Instant loadDate;

  private AssocDao(Builder builder) {
    this.id = builder.id;
    this.stationCode = builder.stationCode;
    this.phase = builder.phase;
    this.belief = builder.belief;
    this.delta = builder.delta;
    this.stationToEventAzimuth = builder.stationToEventAzimuth;
    this.eventToStationAzimuth = builder.eventToStationAzimuth;
    this.timeResidual = builder.timeResidual;
    this.timeDefining = builder.timeDefining;
    this.azimuthResidual = builder.azimuthResidual;
    this.azimuthDefining = builder.azimuthDefining;
    this.slownessResidual = builder.slownessResidual;
    this.slownessDefining = builder.slownessDefining;
    this.emergenceAngleResidual = builder.emergenceAngleResidual;
    this.locationWeight = builder.locationWeight;
    this.velocityModel = builder.velocityModel;
    this.commentId = builder.commentId;
    this.loadDate = builder.loadDate;
  }

  public AssocDao() {

  }

  @EmbeddedId
  public AridOridKey getId() {
    return id;
  }

  public void setId(AridOridKey id) {
    this.id = id;
  }

  @Column(name = "sta", nullable = false)
  public String getStationCode() {
    return stationCode;
  }

  public void setStationCode(String stationCode) {
    this.stationCode = stationCode;
  }

  @Column(name = "phase", nullable = false)
  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  @Column(name = "belief", nullable = false)
  public double getBelief() {
    return belief;
  }

  public void setBelief(double belief) {
    this.belief = belief;
  }

  @Column(name = "delta", nullable = false)
  public double getDelta() {
    return delta;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }

  @Column(name = "seaz", nullable = false)
  public double getStationToEventAzimuth() {
    return stationToEventAzimuth;
  }

  public void setStationToEventAzimuth(double stationToEventAzimuth) {
    this.stationToEventAzimuth = stationToEventAzimuth;
  }

  @Column(name = "esaz", nullable = false)
  public double getEventToStationAzimuth() {
    return eventToStationAzimuth;
  }

  public void setEventToStationAzimuth(double eventToStationAzimuth) {
    this.eventToStationAzimuth = eventToStationAzimuth;
  }

  @Column(name = "timeres", nullable = false)
  public double getTimeResidual() {
    return timeResidual;
  }

  public void setTimeResidual(double timeResidual) {
    this.timeResidual = timeResidual;
  }

  @Column(name = "timedef", nullable = false)
  @Convert(converter = DefiningFlagConverter.class)
  public DefiningFlag getTimeDefining() {
    return timeDefining;
  }

  public void setTimeDefining(DefiningFlag timeDefining) {
    this.timeDefining = timeDefining;
  }

  @Column(name = "azres", nullable = false)
  public double getAzimuthResidual() {
    return azimuthResidual;
  }

  public void setAzimuthResidual(double azimuthResidual) {
    this.azimuthResidual = azimuthResidual;
  }

  @Column(name = "azdef", nullable = false)
  @Convert(converter = DefiningFlagConverter.class)
  public DefiningFlag getAzimuthDefining() {
    return azimuthDefining;
  }

  public void setAzimuthDefining(DefiningFlag azimuthDefining) {
    this.azimuthDefining = azimuthDefining;
  }

  @Column(name = "slores", nullable = false)
  public double getSlownessResidual() {
    return slownessResidual;
  }

  public void setSlownessResidual(double slownessResidual) {
    this.slownessResidual = slownessResidual;
  }

  @Column(name = "slodef", nullable = false)
  @Convert(converter = DefiningFlagConverter.class)
  public DefiningFlag getSlownessDefining() {
    return slownessDefining;
  }

  public void setSlownessDefining(DefiningFlag slownessDefining) {
    this.slownessDefining = slownessDefining;
  }

  @Column(name = "emares", nullable = false)
  public double getEmergenceAngleResidual() {
    return emergenceAngleResidual;
  }

  public void setEmergenceAngleResidual(double emergenceAngleResidual) {
    this.emergenceAngleResidual = emergenceAngleResidual;
  }

  @Column(name = "wgt", nullable = false)
  public double getLocationWeight() {
    return locationWeight;
  }

  public void setLocationWeight(double locationWeight) {
    this.locationWeight = locationWeight;
  }

  @Column(name = "vmodel", nullable = false)
  public String getVelocityModel() {
    return velocityModel;
  }

  public void setVelocityModel(String velocityModel) {
    this.velocityModel = velocityModel;
  }

  @Column(name = "commid", nullable = false)
  public long getCommentId() {
    return commentId;
  }

  public void setCommentId(long commentId) {
    this.commentId = commentId;
  }

  @Column(name = "lddate", nullable = false)
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
    if (!(o instanceof AssocDao)) {
      return false;
    }
    AssocDao assocDao = (AssocDao) o;
    return Double.compare(assocDao.belief, belief) == 0
      && Double.compare(assocDao.delta, delta) == 0
      && Double.compare(assocDao.stationToEventAzimuth, stationToEventAzimuth) == 0
      && Double.compare(assocDao.eventToStationAzimuth, eventToStationAzimuth) == 0
      && Double.compare(assocDao.timeResidual, timeResidual) == 0
      && Double.compare(assocDao.azimuthResidual, azimuthResidual) == 0
      && Double.compare(assocDao.slownessResidual, slownessResidual) == 0
      && Double.compare(assocDao.emergenceAngleResidual, emergenceAngleResidual) == 0
      && Double.compare(assocDao.locationWeight, locationWeight) == 0
      && commentId == assocDao.commentId && Objects.equals(id, assocDao.id)
      && Objects.equals(stationCode, assocDao.stationCode) && Objects.equals(
      phase, assocDao.phase) && timeDefining == assocDao.timeDefining
      && azimuthDefining == assocDao.azimuthDefining
      && slownessDefining == assocDao.slownessDefining && Objects.equals(velocityModel,
      assocDao.velocityModel) && Objects.equals(loadDate, assocDao.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, stationCode, phase, belief, delta, stationToEventAzimuth,
      eventToStationAzimuth, timeResidual, timeDefining, azimuthResidual, azimuthDefining,
      slownessResidual, slownessDefining, emergenceAngleResidual, locationWeight, velocityModel,
      commentId, loadDate);
  }

  @Override
  public String toString() {
    return "AssocDao{" +
      "id=" + id +
      ", stationCode='" + stationCode + '\'' +
      ", phase='" + phase + '\'' +
      ", belief=" + belief +
      ", delta=" + delta +
      ", stationToEventAzimuth=" + stationToEventAzimuth +
      ", eventToStationAzimuth=" + eventToStationAzimuth +
      ", timeResidual=" + timeResidual +
      ", timeDefining=" + timeDefining +
      ", azimuthResidual=" + azimuthResidual +
      ", azimuthDefining=" + azimuthDefining +
      ", slownessResidual=" + slownessResidual +
      ", slownessDefining=" + slownessDefining +
      ", emergenceAngleResidual=" + emergenceAngleResidual +
      ", locationWeight=" + locationWeight +
      ", velocityModel='" + velocityModel + '\'' +
      ", commentId=" + commentId +
      ", loadDate=" + loadDate +
      '}';
  }

  /**
   * A utility for generating a unique {@link AridOridKey} for a provided {@link AssocDao}
   *
   * @param assocDao to transform
   * @return an AridOridKey unique to the provided AssocDao
   */
  public static AridOridKey assocDaoToAridOridKeyTransformer(AssocDao assocDao) {
    var key = new AridOridKey();
    key.setOriginId(assocDao.getId().getOriginId());
    key.setArrivalId(assocDao.getId().getArrivalId());
    return key;
  }

  public static class Builder {
    private AridOridKey id;
    private String stationCode;
    private String phase;
    private double belief;
    private double delta;
    private double stationToEventAzimuth;
    private double eventToStationAzimuth;
    private double timeResidual;
    private DefiningFlag timeDefining;
    private double azimuthResidual;
    private DefiningFlag azimuthDefining;
    private double slownessResidual;
    private DefiningFlag slownessDefining;
    private double emergenceAngleResidual;
    private double locationWeight;
    private String velocityModel;
    private long commentId;
    private Instant loadDate;

    public static Builder initializeFromInstance(AssocDao assocDao) {
      return new AssocDao.Builder()
        .withId(
          new AridOridKey.Builder()
            .withOriginId(assocDao.getId().getOriginId())
            .withArrivalId(assocDao.getId().getArrivalId())
            .build()
        )
        .withStationCode(assocDao.stationCode)
        .withPhase(assocDao.phase)
        .withBelief(assocDao.belief)
        .withDelta(assocDao.delta)
        .withStationToEventAzimuth(assocDao.stationToEventAzimuth)
        .withEventToStationAzimuth(assocDao.eventToStationAzimuth)
        .withTimeResidual(assocDao.timeResidual)
        .withTimeDefining(assocDao.timeDefining)
        .withAzimuthResidual(assocDao.azimuthResidual)
        .withAzimuthDefining(assocDao.azimuthDefining)
        .withSlownessResidual(assocDao.slownessResidual)
        .withSlownessDefining(assocDao.slownessDefining)
        .withEmergenceAngleResidual(assocDao.emergenceAngleResidual)
        .withLocationWeight(assocDao.locationWeight)
        .withVelocityModel(assocDao.velocityModel)
        .withCommentId(assocDao.commentId)
        .withLoadDate(assocDao.loadDate);
    }

    public Builder withId(AridOridKey id) {
      this.id = id;
      return this;
    }

    public Builder withStationCode(String stationCode) {
      this.stationCode = stationCode;
      return this;
    }

    public Builder withPhase(String phase) {
      this.phase = phase;
      return this;
    }

    public Builder withBelief(double belief) {
      this.belief = belief;
      return this;
    }

    public Builder withDelta(double delta) {
      this.delta = delta;
      return this;
    }

    public Builder withStationToEventAzimuth(double stationToEventAzimuth) {
      this.stationToEventAzimuth = stationToEventAzimuth;
      return this;
    }

    public Builder withEventToStationAzimuth(double eventToStationAzimuth) {
      this.eventToStationAzimuth = eventToStationAzimuth;
      return this;
    }

    public Builder withTimeResidual(double timeResidual) {
      this.timeResidual = timeResidual;
      return this;
    }

    public Builder withTimeDefining(DefiningFlag timeDefining) {
      this.timeDefining = timeDefining;
      return this;
    }

    public Builder withAzimuthResidual(double azimuthResidual) {
      this.azimuthResidual = azimuthResidual;
      return this;
    }

    public Builder withAzimuthDefining(DefiningFlag azimuthDefining) {
      this.azimuthDefining = azimuthDefining;
      return this;
    }

    public Builder withSlownessResidual(double slownessResidual) {
      this.slownessResidual = slownessResidual;
      return this;
    }

    public Builder withSlownessDefining(DefiningFlag slownessDefining) {
      this.slownessDefining = slownessDefining;
      return this;
    }

    public Builder withEmergenceAngleResidual(double emergenceAngleResidual) {
      this.emergenceAngleResidual = emergenceAngleResidual;
      return this;
    }

    public Builder withLocationWeight(double locationWeight) {
      this.locationWeight = locationWeight;
      return this;
    }

    public Builder withVelocityModel(String velocityModel) {
      this.velocityModel = velocityModel;
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

    public AssocDao build() {
      return new AssocDao(this);
    }
  }
}
