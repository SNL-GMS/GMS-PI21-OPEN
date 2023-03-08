package gms.core.performancemonitoring.uimaterializedview;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;

import java.util.List;

@AutoValue
public abstract class UIStationAndStationGroupsChanges {


  public abstract List<UnacknowledgedSohStatusChange> getUnacknowledgedStatusChanges();

  public abstract List<QuietedSohStatusChangeUpdate> getQuietedSohStatusChanges();

  public static UIStationAndStationGroupsChanges.Builder builder() {
    return new AutoValue_UIStationAndStationGroupsChanges.Builder();
  }

  public abstract UIStationAndStationGroupsChanges.Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract UIStationAndStationGroupsChanges.Builder setUnacknowledgedStatusChanges(
      List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges);

    public abstract UIStationAndStationGroupsChanges.Builder setQuietedSohStatusChanges(
      List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges);


    abstract UIStationAndStationGroupsChanges autoBuild();

    public UIStationAndStationGroupsChanges build() {
      return autoBuild();
    }

  }

}
