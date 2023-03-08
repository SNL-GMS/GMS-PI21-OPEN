package gms.shared.signaldetection.repository.utils;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SignalDetectionHypothesisAssocIdComponents {

  public abstract String getLegacyDatabaseAccountId();

  public abstract long getArid();

  public abstract long getOrid();

  public static SignalDetectionHypothesisAssocIdComponents create(String legacyDatabaseAccountId, long arid,
    long orid) {
    return new AutoValue_SignalDetectionHypothesisAssocIdComponents(legacyDatabaseAccountId, arid, orid);
  }
}
