package gms.shared.signaldetection.converter.measurementvalue.specs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.stationdefinition.coi.utils.Units;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

@AutoValue
@JsonSerialize(as = MeasurementValueSpec.class)
@JsonDeserialize(builder = AutoValue_MeasurementValueSpec.Builder.class)
public abstract class MeasurementValueSpec<V> implements Serializable {

  public abstract FeatureMeasurementType<V> getFeatureMeasurementType();

  public abstract ArrivalDao getArrivalDao();

  public abstract Optional<String> getFeatureMeasurementTypeCode();

  public abstract Optional<ToDoubleFunction<ArrivalDao>> getMeasuredValueExtractor();

  public abstract Optional<ToDoubleFunction<ArrivalDao>> getUncertaintyValueExtractor();

  public abstract Optional<AssocDao> getAssocDao();

  public abstract Optional<AmplitudeDao> getAmplitudeDao();

  public abstract Optional<Units> getUnits();

  public static <V> Builder<V> builder() {
    return new AutoValue_MeasurementValueSpec.Builder<>();
  }

  public abstract Builder<V> toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder<V> {

    public abstract Builder<V> setFeatureMeasurementType(FeatureMeasurementType<V> featureMeasurementType);

    public abstract Builder<V> setArrivalDao(ArrivalDao arrivalDao);

    public abstract Builder<V> setFeatureMeasurementTypeCode(Optional<String> featureMeasurementTypeCode);

    public abstract Builder<V> setFeatureMeasurementTypeCode(String featureMeasurementTypeCode);

    public abstract Builder<V> setMeasuredValueExtractor(Optional<ToDoubleFunction<ArrivalDao>> measuredValueExtractor);

    public abstract Builder<V> setMeasuredValueExtractor(ToDoubleFunction<ArrivalDao> measuredValueExtractor);

    public abstract Builder<V> setUncertaintyValueExtractor(
      Optional<ToDoubleFunction<ArrivalDao>> uncertaintyValueExtractor);

    public abstract Builder<V> setUncertaintyValueExtractor(ToDoubleFunction<ArrivalDao> uncertaintyValueExtractor);

    public abstract Builder<V> setAssocDao(Optional<AssocDao> assocDao);

    public abstract Builder<V> setAssocDao(AssocDao assocDao);

    public abstract Builder<V> setAmplitudeDao(Optional<AmplitudeDao> amplitudeDao);

    public abstract Builder<V> setAmplitudeDao(AmplitudeDao amplitudeDao);

    public abstract Builder<V> setUnits(Optional<Units> units);

    public abstract Builder<V> setUnits(Units units);

    abstract MeasurementValueSpec<V> autoBuild();

    public MeasurementValueSpec<V> build() {
      return autoBuild();
    }
  }

}
