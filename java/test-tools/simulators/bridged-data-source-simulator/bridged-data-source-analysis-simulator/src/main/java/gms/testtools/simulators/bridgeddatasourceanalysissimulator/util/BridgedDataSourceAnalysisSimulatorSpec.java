package gms.testtools.simulators.bridgeddatasourceanalysissimulator.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;

import java.io.Serializable;
import java.util.Map;

@AutoValue
@JsonSerialize(as = BridgedDataSourceAnalysisSimulatorSpec.class)
@JsonDeserialize(builder = AutoValue_BridgedDataSourceAnalysisSimulatorSpec.class)
public abstract class BridgedDataSourceAnalysisSimulatorSpec implements Serializable {
  public abstract Map<String, ArrivalDatabaseConnector> getArrivalDatabaseConnectorMap();

  public abstract Map<String, AmplitudeDatabaseConnector> getAmplitudeDatabaseConnectorMap();

  public abstract Map<String, EventDatabaseConnector> getEventDatabaseConnectorMap();

  public abstract Map<String, OriginErrDatabaseConnector> getOriginErrDatabaseConnectorMap();

  public abstract Map<String, OriginSimulatorDatabaseConnector> getOriginSimulatorDatabaseConnectorMap();

  public abstract WfdiscDatabaseConnector getWfdiscDatabaseConnector();

  public abstract WftagDatabaseConnector getWftagDatabaseConnector();

  public abstract BeamDatabaseConnector getBeamDatabaseConnector();

  public abstract Map<String, BridgedDataSourceRepository> getSignalDetectionBridgedDataSourceRepositoryMap();

  public abstract Map<String, BridgedDataSourceRepository> getOriginBridgedDataSourceRepositoryMap();

  public abstract BridgedDataSourceRepository getWaveformBridgedDataSourceRepository();

  public abstract BridgedDataSourceRepository getWftagBridgedDataSourceRepository();

  public static Builder builder() {
    return new AutoValue_BridgedDataSourceAnalysisSimulatorSpec.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    public abstract Builder setArrivalDatabaseConnectorMap(
      Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap);

    public abstract Builder setAmplitudeDatabaseConnectorMap(
      Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap);

    public abstract Builder setEventDatabaseConnectorMap(Map<String, EventDatabaseConnector> eventDatabaseConnectorMap);

    public abstract Builder setOriginErrDatabaseConnectorMap(
      Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap);

    public abstract Builder setOriginSimulatorDatabaseConnectorMap(
      Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap);

    public abstract Builder setWfdiscDatabaseConnector(WfdiscDatabaseConnector wfdiscDatabaseConnector);

    public abstract Builder setWftagDatabaseConnector(WftagDatabaseConnector wftagDatabaseConnector);

    public abstract Builder setBeamDatabaseConnector(BeamDatabaseConnector beamDatabaseConnector);

    public abstract Builder setSignalDetectionBridgedDataSourceRepositoryMap(
      Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap);

    public abstract Builder setOriginBridgedDataSourceRepositoryMap(
      Map<String, BridgedDataSourceRepository> originBridgedDataSourceRepositoryMap);

    public abstract Builder setWaveformBridgedDataSourceRepository(
      BridgedDataSourceRepository waveformBridgedDataSourceRepository);

    public abstract Builder setWftagBridgedDataSourceRepository(
      BridgedDataSourceRepository wftagBridgedDataSourceRepository);

    abstract BridgedDataSourceAnalysisSimulatorSpec autoBuild();

    public BridgedDataSourceAnalysisSimulatorSpec build() {
      return autoBuild();
    }
  }
}
