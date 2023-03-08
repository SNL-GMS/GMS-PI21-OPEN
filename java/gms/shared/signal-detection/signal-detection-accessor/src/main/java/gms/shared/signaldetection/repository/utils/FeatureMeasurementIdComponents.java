package gms.shared.signaldetection.repository.utils;

import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;

import java.util.UUID;

@AutoValue
public abstract class FeatureMeasurementIdComponents {

  public abstract UUID getSignalDetectionHypotheisId();

  public abstract FeatureMeasurementType getFeatureMeasurementType();

  public static FeatureMeasurementIdComponents create(UUID signalDetectionHypothesisId,
    FeatureMeasurementType featureMeasurementType) {
    return new AutoValue_FeatureMeasurementIdComponents(signalDetectionHypothesisId, featureMeasurementType);
  }
}
