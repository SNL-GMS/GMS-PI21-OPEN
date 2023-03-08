package gms.dataacquisition.stationreceiver.cd11.common.gaps;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Cd11GapManagementBundle {

  public abstract Cd11GapList getCd11GapList();

  public abstract Cd11GapListUtility getCd11GapListUtility();

  public abstract String getGapListStoragePath();

  public abstract int getGapExpirationDays();

  public abstract long getGapStorageIntervalMinutes();

  public static Builder builder() {
    return new AutoValue_Cd11GapManagementBundle.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Cd11GapManagementBundle.Builder setCd11GapList(Cd11GapList cd11GapList);

    public abstract Cd11GapManagementBundle.Builder setCd11GapListUtility(Cd11GapListUtility cd11GapListUtility);

    public abstract Cd11GapManagementBundle.Builder setGapListStoragePath(String gapListStoragePath);

    public abstract Cd11GapManagementBundle.Builder setGapExpirationDays(int gapExpirationDays);

    public abstract Cd11GapManagementBundle.Builder setGapStorageIntervalMinutes(long gapStorageIntervalMinutes);

    public abstract Cd11GapManagementBundle build();
  }
}
