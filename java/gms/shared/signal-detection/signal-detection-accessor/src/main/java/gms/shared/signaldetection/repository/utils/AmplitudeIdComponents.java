package gms.shared.signaldetection.repository.utils;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AmplitudeIdComponents {

  public abstract String getLegacyDatabaseAccountId();

  public abstract long getAmpid();

  public static AmplitudeIdComponents create(String legacyDatabaseAccountId, long ampid) {
    return new AutoValue_AmplitudeIdComponents(legacyDatabaseAccountId, ampid);
  }
}
