package gms.shared.event.dao;

import gms.shared.utilities.bridge.database.converter.DoubleConverter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a record in the legacy event_control table.
 */
@Entity
@Table(name = "event_control")
public class EventControlDao {

  private EventIdOriginIdKey eventIdOriginIdKey;
  private String preferredLocation;
  private boolean constrainOriginTime;
  private boolean constrainLatLon;
  private boolean constrainDepth;
  private int sourceDependentCorrectionCode;
  private String sourceDependentLocationCorrectionRegion;
  private boolean ignoreLargeResidualsInLocation;
  private double locationLargeResidualMultiplier;
  private boolean useStationSubsetInLocation;
  private boolean useAllStationsInLocation;
  private boolean useDistanceVarianceWeighting;
  private double userDefinedDistanceVarianceWeighting;
  private String sourceDependentMagnitudeCorrectionRegion;
  private boolean ignoreLargeResidualsInMagnitude;
  private double magnitudeLargeResidualMultiplier;
  private boolean useStationSubsetInMagnitude;
  private boolean useAllStationsInMagnitude;
  private double mbMinimumDistance;
  private double mbMaximumDistance;
  private String magnitudeModel;
  private double ellipseSemiaxisConversionFactor;
  private double ellipseDepthTimeConversionFactor;
  private Instant loadDate;

  public EventControlDao() {

  }

  private EventControlDao(Builder builder) {
    this.eventIdOriginIdKey = builder.eventIdOriginIdKey;
    this.preferredLocation = builder.preferredLocation;
    this.constrainOriginTime = builder.constrainOriginTime;
    this.constrainLatLon = builder.constrainLatLon;
    this.constrainDepth = builder.constrainDepth;
    this.sourceDependentCorrectionCode = builder.sourceDependentCorrectionCode;
    this.sourceDependentLocationCorrectionRegion = builder.sourceDependentLocationCorrectionRegion;
    this.ignoreLargeResidualsInLocation = builder.ignoreLargeResidualsInLocation;
    this.locationLargeResidualMultiplier = builder.locationLargeResidualMultiplier;
    this.useStationSubsetInLocation = builder.useStationSubsetInLocation;
    this.useAllStationsInLocation = builder.useAllStationsInLocation;
    this.useDistanceVarianceWeighting = builder.useDistanceVarianceWeighting;
    this.userDefinedDistanceVarianceWeighting = builder.userDefinedDistanceVarianceWeighting;
    this.sourceDependentMagnitudeCorrectionRegion = builder.sourceDependentMagnitudeCorrectionRegion;
    this.ignoreLargeResidualsInMagnitude = builder.ignoreLargeResidualsInMagnitude;
    this.magnitudeLargeResidualMultiplier = builder.magnitudeLargeResidualMultiplier;
    this.useStationSubsetInMagnitude = builder.useStationSubsetInMagnitude;
    this.useAllStationsInMagnitude = builder.useAllStationsInMagnitude;
    this.mbMinimumDistance = builder.mbMinimumDistance;
    this.mbMaximumDistance = builder.mbMaximumDistance;
    this.magnitudeModel = builder.magnitudeModel;
    this.ellipseSemiaxisConversionFactor = builder.ellipseSemiaxisConversionFactor;
    this.ellipseDepthTimeConversionFactor = builder.ellipseDepthTimeConversionFactor;
    this.loadDate = builder.loadDate;
  }

  @EmbeddedId
  public EventIdOriginIdKey getEventIdOriginIdKey() {
    return eventIdOriginIdKey;
  }

  public void setEventIdOriginIdKey(EventIdOriginIdKey eventIdOriginIdKey) {
    this.eventIdOriginIdKey = eventIdOriginIdKey;
  }

  @Transient
  public long getEventId() {
    return eventIdOriginIdKey.getEventId();
  }

  @Transient
  public long getOriginId() {
    return eventIdOriginIdKey.getOriginId();
  }

  @Column(name = "prefer_loc")
  public String getPreferredLocation() {
    return preferredLocation;
  }

  public void setPreferredLocation(String preferredLocation) {
    this.preferredLocation = preferredLocation;
  }

  @Column(name = "constrain_ot")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getConstrainOriginTime() {
    return constrainOriginTime;
  }

  public void setConstrainOriginTime(boolean constrainOriginTime) {
    this.constrainOriginTime = constrainOriginTime;
  }

  @Column(name = "constrain_latlon")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getConstrainLatLon() {
    return constrainLatLon;
  }

  public void setConstrainLatLon(boolean constrainLatLon) {
    this.constrainLatLon = constrainLatLon;
  }

  @Column(name = "constrain_depth")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getConstrainDepth() {
    return constrainDepth;
  }

  public void setConstrainDepth(boolean constrainDepth) {
    this.constrainDepth = constrainDepth;
  }

  @Column(name = "src_dpnt_corr")
  public int getSourceDependentCorrectionCode() {
    return sourceDependentCorrectionCode;
  }

  public void setSourceDependentCorrectionCode(int sourceDependentCorrectionCode) {
    this.sourceDependentCorrectionCode = sourceDependentCorrectionCode;
  }

  @Column(name = "loc_src_dpnt_reg")
  public String getSourceDependentLocationCorrectionRegion() {
    return sourceDependentLocationCorrectionRegion;
  }

  public void setSourceDependentLocationCorrectionRegion(
    String sourceDependentLocationCorrectionRegion) {
    this.sourceDependentLocationCorrectionRegion = sourceDependentLocationCorrectionRegion;
  }

  @Column(name = "loc_sdv_screen")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getIgnoreLargeResidualsInLocation() {
    return ignoreLargeResidualsInLocation;
  }

  public void setIgnoreLargeResidualsInLocation(boolean ignoreLargeResidualsInLocation) {
    this.ignoreLargeResidualsInLocation = ignoreLargeResidualsInLocation;
  }

  @Column(name = "loc_sdv_mult")
  public double getLocationLargeResidualMultiplier() {
    return locationLargeResidualMultiplier;
  }

  public void setLocationLargeResidualMultiplier(double locationLargeResidualMultiplier) {
    this.locationLargeResidualMultiplier = locationLargeResidualMultiplier;
  }

  @Column(name = "loc_alpha_only")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getUseStationSubsetInLocation() {
    return useStationSubsetInLocation;
  }

  public void setUseStationSubsetInLocation(boolean useStationSubsetInLocation) {
    this.useStationSubsetInLocation = useStationSubsetInLocation;
  }

  @Column(name = "loc_all_stas")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getUseAllStationsInLocation() {
    return useAllStationsInLocation;
  }

  public void setUseAllStationsInLocation(
    boolean useAllStationsInLocation) {
    this.useAllStationsInLocation = useAllStationsInLocation;
  }

  @Column(name = "loc_dist_varwgt")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getUseDistanceVarianceWeighting() {
    return useDistanceVarianceWeighting;
  }

  public void setUseDistanceVarianceWeighting(boolean useDistanceVarianceWeighting) {
    this.useDistanceVarianceWeighting = useDistanceVarianceWeighting;
  }

  @Column(name = "loc_user_varwgt")
  @Convert(converter = DoubleConverter.class)
  public double getUserDefinedDistanceVarianceWeighting() {
    return userDefinedDistanceVarianceWeighting;
  }

  public void setUserDefinedDistanceVarianceWeighting(
    Double userDefinedDistanceVarianceWeighting) {
    this.userDefinedDistanceVarianceWeighting = userDefinedDistanceVarianceWeighting;
  }

  @Column(name = "mag_src_dpnt_reg")
  public String getSourceDependentMagnitudeCorrectionRegion() {
    return sourceDependentMagnitudeCorrectionRegion;
  }

  public void setSourceDependentMagnitudeCorrectionRegion(
    String sourceDependentMagnitudeCorrectionRegion) {
    this.sourceDependentMagnitudeCorrectionRegion = sourceDependentMagnitudeCorrectionRegion;
  }

  @Column(name = "mag_sdv_screen")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getIgnoreLargeResidualsInMagnitude() {
    return ignoreLargeResidualsInMagnitude;
  }

  public void setIgnoreLargeResidualsInMagnitude(boolean ignoreLargeResidualsInMagnitude) {
    this.ignoreLargeResidualsInMagnitude = ignoreLargeResidualsInMagnitude;
  }

  @Column(name = "mag_sdv_mult")
  public double getMagnitudeLargeResidualMultiplier() {
    return magnitudeLargeResidualMultiplier;
  }

  public void setMagnitudeLargeResidualMultiplier(double magnitudeLargeResidualMultiplier) {
    this.magnitudeLargeResidualMultiplier = magnitudeLargeResidualMultiplier;
  }

  @Column(name = "mag_alpha_only")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getUseStationSubsetInMagnitude() {
    return useStationSubsetInMagnitude;
  }

  public void setUseStationSubsetInMagnitude(boolean useStationSubsetInMagnitude) {
    this.useStationSubsetInMagnitude = useStationSubsetInMagnitude;
  }

  @Column(name = "mag_all_stas")
  @Type(type = "org.hibernate.type.NumericBooleanType")
  public boolean getUseAllStationsInMagnitude() {
    return useAllStationsInMagnitude;
  }

  public void setUseAllStationsInMagnitude(boolean useAllStationsInMagnitude) {
    this.useAllStationsInMagnitude = useAllStationsInMagnitude;
  }

  @Column(name = "mb_min_dist")
  public double getMbMinimumDistance() {
    return mbMinimumDistance;
  }

  public void setMbMinimumDistance(double mbMinimumDistance) {
    this.mbMinimumDistance = mbMinimumDistance;
  }

  @Column(name = "mb_max_dist")
  public double getMbMaximumDistance() {
    return mbMaximumDistance;
  }

  public void setMbMaximumDistance(double mbMaximumDistance) {
    this.mbMaximumDistance = mbMaximumDistance;
  }

  @Column(name = "mmodel")
  public String getMagnitudeModel() {
    return magnitudeModel;
  }

  public void setMagnitudeModel(String magnitudeModel) {
    this.magnitudeModel = magnitudeModel;
  }

  @Column(name = "cov_sm_axes")
  public double getEllipseSemiaxisConversionFactor() {
    return ellipseSemiaxisConversionFactor;
  }

  public void setEllipseSemiaxisConversionFactor(double ellipseSemiaxisConversionFactor) {
    this.ellipseSemiaxisConversionFactor = ellipseSemiaxisConversionFactor;
  }

  @Column(name = "cov_depth_time")
  public double getEllipseDepthTimeConversionFactor() {
    return ellipseDepthTimeConversionFactor;
  }

  public void setEllipseDepthTimeConversionFactor(double ellipseDepthTimeConversionFactor) {
    this.ellipseDepthTimeConversionFactor = ellipseDepthTimeConversionFactor;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  public static class Builder {

    private EventIdOriginIdKey eventIdOriginIdKey;
    private String preferredLocation;
    private boolean constrainOriginTime;
    private boolean constrainLatLon;
    private boolean constrainDepth;
    private int sourceDependentCorrectionCode;
    private String sourceDependentLocationCorrectionRegion;
    private boolean ignoreLargeResidualsInLocation;
    private double locationLargeResidualMultiplier;
    private boolean useStationSubsetInLocation;
    private boolean useAllStationsInLocation;
    private boolean useDistanceVarianceWeighting;
    private double userDefinedDistanceVarianceWeighting;
    private String sourceDependentMagnitudeCorrectionRegion;
    private boolean ignoreLargeResidualsInMagnitude;
    private double magnitudeLargeResidualMultiplier;
    private boolean useStationSubsetInMagnitude;
    private boolean useAllStationsInMagnitude;
    private double mbMinimumDistance;
    private double mbMaximumDistance;
    private String magnitudeModel;
    private double ellipseSemiaxisConversionFactor;
    private double ellipseDepthTimeConversionFactor;
    private Instant loadDate;

    public static EventControlDao.Builder initializeFromInstance(EventControlDao dao) {
      return new EventControlDao.Builder()
        .withEventIdOriginIdKey(
          new EventIdOriginIdKey.Builder()
            .withOriginId(dao.getOriginId())
            .withEventId(dao.getEventId())
            .build()
        )
        .withPreferredLocation(dao.preferredLocation)
        .withConstrainOriginTime(dao.constrainOriginTime)
        .withConstrainLatLon(dao.constrainLatLon)
        .withConstrainDepth(dao.constrainDepth)
        .withSourceDependentCorrectionCode(dao.sourceDependentCorrectionCode)
        .withSourceDependentLocationCorrectionRegion(dao.sourceDependentLocationCorrectionRegion)
        .withIgnoreLargeResidualsInLocation(dao.ignoreLargeResidualsInLocation)
        .withLocationLargeResidualMultiplier(dao.locationLargeResidualMultiplier)
        .withUseStationSubsetInLocation(dao.useStationSubsetInLocation)
        .withUseAllStationsInLocation(dao.useAllStationsInLocation)
        .withUseDistanceVarianceWeighting(dao.useDistanceVarianceWeighting)
        .withUserDefinedDistanceVarianceWeighting(dao.userDefinedDistanceVarianceWeighting)
        .withSourceDependentMagnitudeCorrectionRegion(dao.sourceDependentMagnitudeCorrectionRegion)
        .withIgnoreLargeResidualsInMagnitude(dao.ignoreLargeResidualsInMagnitude)
        .withMagnitudeLargeResidualMultiplier(dao.magnitudeLargeResidualMultiplier)
        .withUseStationSubsetInMagnitude(dao.useStationSubsetInMagnitude)
        .withUseAllStationsInMagnitude(dao.useAllStationsInMagnitude)
        .withMbMinimumDistance(dao.mbMinimumDistance)
        .withMbMaximumDistance(dao.mbMaximumDistance)
        .withMagnitudeModel(dao.magnitudeModel)
        .withEllipseSemiaxisConversionFactor(dao.ellipseSemiaxisConversionFactor)
        .withEllipseDepthTimeConversionFactor(dao.ellipseDepthTimeConversionFactor)
        .withLoadDate(dao.loadDate);
    }

    public Builder withEventIdOriginIdKey(EventIdOriginIdKey eventIdOriginIdKey) {
      this.eventIdOriginIdKey = eventIdOriginIdKey;
      return this;
    }

    public Builder withPreferredLocation(String preferredLocation) {
      this.preferredLocation = preferredLocation;
      return this;
    }

    public Builder withConstrainOriginTime(boolean constrainOriginTime) {
      this.constrainOriginTime = constrainOriginTime;
      return this;
    }

    public Builder withConstrainLatLon(boolean constrainLatLon) {
      this.constrainLatLon = constrainLatLon;
      return this;
    }

    public Builder withConstrainDepth(boolean constrainDepth) {
      this.constrainDepth = constrainDepth;
      return this;
    }

    public Builder withSourceDependentCorrectionCode(int sourceDependentCorrectionCode) {
      this.sourceDependentCorrectionCode = sourceDependentCorrectionCode;
      return this;
    }

    public Builder withSourceDependentLocationCorrectionRegion(
      String sourceDependentLocationCorrectionRegion) {
      this.sourceDependentLocationCorrectionRegion = sourceDependentLocationCorrectionRegion;
      return this;
    }

    public Builder withIgnoreLargeResidualsInLocation(boolean ignoreLargeResidualsInLocation) {
      this.ignoreLargeResidualsInLocation = ignoreLargeResidualsInLocation;
      return this;
    }

    public Builder withLocationLargeResidualMultiplier(double locationLargeResidualMultiplier) {
      this.locationLargeResidualMultiplier = locationLargeResidualMultiplier;
      return this;
    }

    public Builder withUseStationSubsetInLocation(boolean useStationSubsetInLocation) {
      this.useStationSubsetInLocation = useStationSubsetInLocation;
      return this;
    }

    public Builder withUseAllStationsInLocation(boolean useAllStationsInLocation) {
      this.useAllStationsInLocation = useAllStationsInLocation;
      return this;
    }

    public Builder withUseDistanceVarianceWeighting(boolean useDistanceVarianceWeighting) {
      this.useDistanceVarianceWeighting = useDistanceVarianceWeighting;
      return this;
    }

    public Builder withUserDefinedDistanceVarianceWeighting(
      double userDefinedDistanceVarianceWeighting) {
      this.userDefinedDistanceVarianceWeighting = userDefinedDistanceVarianceWeighting;
      return this;
    }

    public Builder withSourceDependentMagnitudeCorrectionRegion(
      String sourceDependentMagnitudeCorrectionRegion) {
      this.sourceDependentMagnitudeCorrectionRegion = sourceDependentMagnitudeCorrectionRegion;
      return this;
    }

    public Builder withIgnoreLargeResidualsInMagnitude(boolean ignoreLargeResidualsInMagnitude) {
      this.ignoreLargeResidualsInMagnitude = ignoreLargeResidualsInMagnitude;
      return this;
    }

    public Builder withMagnitudeLargeResidualMultiplier(double magnitudeLargeResidualMultiplier) {
      this.magnitudeLargeResidualMultiplier = magnitudeLargeResidualMultiplier;
      return this;
    }

    public Builder withUseStationSubsetInMagnitude(boolean useStationSubsetInMagnitude) {
      this.useStationSubsetInMagnitude = useStationSubsetInMagnitude;
      return this;
    }

    public Builder withUseAllStationsInMagnitude(boolean useAllStationsInMagnitude) {
      this.useAllStationsInMagnitude = useAllStationsInMagnitude;
      return this;
    }

    public Builder withMbMinimumDistance(double mbMinimumDistance) {
      this.mbMinimumDistance = mbMinimumDistance;
      return this;
    }

    public Builder withMbMaximumDistance(double mbMaximumDistance) {
      this.mbMaximumDistance = mbMaximumDistance;
      return this;
    }

    public Builder withMagnitudeModel(String magnitudeModel) {
      this.magnitudeModel = magnitudeModel;
      return this;
    }

    public Builder withEllipseSemiaxisConversionFactor(double ellipseSemiaxisConversionFactor) {
      this.ellipseSemiaxisConversionFactor = ellipseSemiaxisConversionFactor;
      return this;
    }

    public Builder withEllipseDepthTimeConversionFactor(double ellipseDepthTimeConversionFactor) {
      this.ellipseDepthTimeConversionFactor = ellipseDepthTimeConversionFactor;
      return this;
    }

    public Builder withLoadDate(Instant loadDate) {
      this.loadDate = loadDate;
      return this;
    }

    public EventControlDao build() {
      return new EventControlDao(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventControlDao)) {
      return false;
    }
    EventControlDao that = (EventControlDao) o;
    return constrainOriginTime == that.constrainOriginTime
      && constrainLatLon == that.constrainLatLon
      && constrainDepth == that.constrainDepth
      && sourceDependentCorrectionCode == that.sourceDependentCorrectionCode
      && ignoreLargeResidualsInLocation == that.ignoreLargeResidualsInLocation &&
      Double.compare(that.locationLargeResidualMultiplier,
        locationLargeResidualMultiplier) == 0
      && useStationSubsetInLocation == that.useStationSubsetInLocation
      && useAllStationsInLocation == that.useAllStationsInLocation
      && useDistanceVarianceWeighting == that.useDistanceVarianceWeighting &&
      Double.compare(that.userDefinedDistanceVarianceWeighting,
        userDefinedDistanceVarianceWeighting) == 0
      && ignoreLargeResidualsInMagnitude == that.ignoreLargeResidualsInMagnitude &&
      Double.compare(that.magnitudeLargeResidualMultiplier,
        magnitudeLargeResidualMultiplier) == 0
      && useStationSubsetInMagnitude == that.useStationSubsetInMagnitude
      && useAllStationsInMagnitude == that.useAllStationsInMagnitude
      && Double.compare(that.mbMinimumDistance, mbMinimumDistance) == 0
      && Double.compare(that.mbMaximumDistance, mbMaximumDistance) == 0 &&
      Double.compare(that.ellipseSemiaxisConversionFactor,
        ellipseSemiaxisConversionFactor) == 0 &&
      Double.compare(that.ellipseDepthTimeConversionFactor,
        ellipseDepthTimeConversionFactor) == 0 && Objects.equals(eventIdOriginIdKey,
      that.eventIdOriginIdKey) && Objects.equals(preferredLocation,
      that.preferredLocation) && Objects.equals(sourceDependentLocationCorrectionRegion,
      that.sourceDependentLocationCorrectionRegion) && Objects.equals(
      sourceDependentMagnitudeCorrectionRegion, that.sourceDependentMagnitudeCorrectionRegion)
      && Objects.equals(magnitudeModel, that.magnitudeModel) && Objects.equals(
      loadDate, that.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventIdOriginIdKey, preferredLocation, constrainOriginTime, constrainLatLon,
      constrainDepth, sourceDependentCorrectionCode, sourceDependentLocationCorrectionRegion,
      ignoreLargeResidualsInLocation, locationLargeResidualMultiplier, useStationSubsetInLocation,
      useAllStationsInLocation, useDistanceVarianceWeighting,
      userDefinedDistanceVarianceWeighting,
      sourceDependentMagnitudeCorrectionRegion, ignoreLargeResidualsInMagnitude,
      magnitudeLargeResidualMultiplier, useStationSubsetInMagnitude, useAllStationsInMagnitude,
      mbMinimumDistance, mbMaximumDistance, magnitudeModel, ellipseSemiaxisConversionFactor,
      ellipseDepthTimeConversionFactor, loadDate);
  }

  @Override
  public String toString() {
    return "EventControlDao{" +
      "eventIdOriginIdKey=" + eventIdOriginIdKey +
      ", preferredLocation='" + preferredLocation + '\'' +
      ", constrainOriginTime=" + constrainOriginTime +
      ", constrainLatLon=" + constrainLatLon +
      ", constrainDepth=" + constrainDepth +
      ", sourceDependentCorrectionCode=" + sourceDependentCorrectionCode +
      ", sourceDependentLocationCorrectionRegion='" + sourceDependentLocationCorrectionRegion
      + '\''
      +
      ", ignoreLargeResidualsInLocation=" + ignoreLargeResidualsInLocation +
      ", locationLargeResidualMultiplier=" + locationLargeResidualMultiplier +
      ", useStationSubsetInLocation=" + useStationSubsetInLocation +
      ", useAllStationsInLocation=" + useAllStationsInLocation +
      ", useDistanceVarianceWeighting=" + useDistanceVarianceWeighting +
      ", userDefinedDistanceVarianceWeighting=" + userDefinedDistanceVarianceWeighting +
      ", sourceDependentMagnitudeCorrectionRegion='" + sourceDependentMagnitudeCorrectionRegion
      + '\'' +
      ", ignoreLargeResidualsInMagnitude=" + ignoreLargeResidualsInMagnitude +
      ", magnitudeLargeResidualMultiplier=" + magnitudeLargeResidualMultiplier +
      ", useStationSubsetInMagnitude=" + useStationSubsetInMagnitude +
      ", useAllStationsInMagnitude=" + useAllStationsInMagnitude +
      ", mbMinimumDistance=" + mbMinimumDistance +
      ", mbMaximumDistance=" + mbMaximumDistance +
      ", magnitudeModel='" + magnitudeModel + '\'' +
      ", ellipseSemiaxisConversionFactor=" + ellipseSemiaxisConversionFactor +
      ", ellipseDepthTimeConversionFactor=" + ellipseDepthTimeConversionFactor +
      ", loadDate=" + loadDate +
      '}';
  }
}
