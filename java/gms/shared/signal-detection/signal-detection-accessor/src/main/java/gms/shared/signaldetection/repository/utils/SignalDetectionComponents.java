package gms.shared.signaldetection.repository.utils;

import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.Collection;
import java.util.Optional;

@AutoValue
public abstract class SignalDetectionComponents {

  public abstract WorkflowDefinitionId getCurrentStage();

  public abstract Optional<WorkflowDefinitionId> getPreviousStage();

  public abstract ArrivalDao getCurrentArrival();

  public abstract Optional<ArrivalDao> getPreviousArrival();

  public abstract Collection<AssocDao> getCurrentAssocs();

  public abstract Collection<AssocDao> getPreviousAssocs();

  public abstract Collection<AmplitudeDao> getAmplitudeDaos();

  public abstract Station getStation();

  public abstract String getMonitoringOrganization();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_SignalDetectionComponents.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setCurrentStage(WorkflowDefinitionId currentStage);

    public abstract Builder setPreviousStage(Optional<WorkflowDefinitionId> previousStage);

    public abstract Builder setCurrentArrival(ArrivalDao currentArrival);

    public abstract Builder setPreviousArrival(Optional<ArrivalDao> previousArrival);

    public abstract Builder setCurrentAssocs(Collection<AssocDao> currentAssocs);

    public abstract Builder setPreviousAssocs(Collection<AssocDao> previousAssocs);

    public abstract Builder setAmplitudeDaos(Collection<AmplitudeDao> amplitudeDaos);

    public abstract Builder setStation(Station station);

    public abstract Builder setMonitoringOrganization(String monitoringOrganization);

    public abstract SignalDetectionComponents build();
  }

}
