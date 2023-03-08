package gms.shared.signaldetection.repository.utils;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SignalDetectionHypothesisArrivalIdComponents {

  public abstract String getLegacyDatabaseAccountId();

  public abstract long getArid();

  public static SignalDetectionHypothesisArrivalIdComponents create(String legacyDatabaseAccountId, long arid) {
    return new AutoValue_SignalDetectionHypothesisArrivalIdComponents(legacyDatabaseAccountId, arid);
  }

}
