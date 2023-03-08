package gms.testtools.simulators.bridgeddatasourceanalysissimulator.util;

import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BridgedDataSourceAnalysisSimulatorSpecTest {

  @Mock
  private Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap;
  @Mock
  private Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap;
  @Mock
  private Map<String, EventDatabaseConnector> eventDatabaseConnectorMap;
  @Mock
  private Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap;
  @Mock
  private Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap;
  @Mock
  private WfdiscDatabaseConnector wfdiscDatabaseConnector;
  @Mock
  private WftagDatabaseConnector wftagDatabaseConnector;
  @Mock
  private BeamDatabaseConnector beamDatabaseConnector;
  @Mock
  private Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap;
  @Mock
  private Map<String, BridgedDataSourceRepository> originBridgedDataSourceRepositoryMap;
  @Mock
  private BridgedDataSourceRepository waveformBridgedDataSourceRepository;
  @Mock
  private BridgedDataSourceRepository wftagBridgedDataSourceRepository;

  private BridgedDataSourceAnalysisSimulatorSpec.Builder builder;

  @BeforeEach
  public void testSetup() {
    builder = BridgedDataSourceAnalysisSimulatorSpec.builder();
  }

  @Test
  void successfulValidation() {
    assertDoesNotThrow(() -> builder
      .setArrivalDatabaseConnectorMap(arrivalDatabaseConnectorMap)
      .setAmplitudeDatabaseConnectorMap(amplitudeDatabaseConnectorMap)
      .setEventDatabaseConnectorMap(eventDatabaseConnectorMap)
      .setOriginErrDatabaseConnectorMap(originErrDatabaseConnectorMap)
      .setOriginSimulatorDatabaseConnectorMap(originSimulatorDatabaseConnectorMap)
      .setWfdiscDatabaseConnector(wfdiscDatabaseConnector)
      .setWftagDatabaseConnector(wftagDatabaseConnector)
      .setBeamDatabaseConnector(beamDatabaseConnector)
      .setSignalDetectionBridgedDataSourceRepositoryMap(signalDetectionBridgedDataSourceRepositoryMap)
      .setOriginBridgedDataSourceRepositoryMap(originBridgedDataSourceRepositoryMap)
      .setWaveformBridgedDataSourceRepository(waveformBridgedDataSourceRepository)
      .setWftagBridgedDataSourceRepository(wftagBridgedDataSourceRepository)
      .build());
  }

  @Test
  void testSerializationDeserialization() {
    var simSpec = builder
      .setArrivalDatabaseConnectorMap(arrivalDatabaseConnectorMap)
      .setAmplitudeDatabaseConnectorMap(amplitudeDatabaseConnectorMap)
      .setEventDatabaseConnectorMap(eventDatabaseConnectorMap)
      .setOriginErrDatabaseConnectorMap(originErrDatabaseConnectorMap)
      .setOriginSimulatorDatabaseConnectorMap(originSimulatorDatabaseConnectorMap)
      .setWfdiscDatabaseConnector(wfdiscDatabaseConnector)
      .setWftagDatabaseConnector(wftagDatabaseConnector)
      .setBeamDatabaseConnector(beamDatabaseConnector)
      .setSignalDetectionBridgedDataSourceRepositoryMap(signalDetectionBridgedDataSourceRepositoryMap)
      .setOriginBridgedDataSourceRepositoryMap(originBridgedDataSourceRepositoryMap)
      .setWaveformBridgedDataSourceRepository(waveformBridgedDataSourceRepository)
      .setWftagBridgedDataSourceRepository(wftagBridgedDataSourceRepository)
      .build();
    assertEquals(arrivalDatabaseConnectorMap, simSpec.getArrivalDatabaseConnectorMap());
    assertEquals(amplitudeDatabaseConnectorMap, simSpec.getAmplitudeDatabaseConnectorMap());
    assertEquals(eventDatabaseConnectorMap, simSpec.getEventDatabaseConnectorMap());
    assertEquals(originErrDatabaseConnectorMap, simSpec.getOriginErrDatabaseConnectorMap());
    assertEquals(originSimulatorDatabaseConnectorMap, simSpec.getOriginSimulatorDatabaseConnectorMap());
    assertEquals(wfdiscDatabaseConnector, simSpec.getWfdiscDatabaseConnector());
    assertEquals(wftagDatabaseConnector, simSpec.getWftagDatabaseConnector());
    assertEquals(beamDatabaseConnector, simSpec.getBeamDatabaseConnector());
    assertEquals(signalDetectionBridgedDataSourceRepositoryMap, simSpec.getSignalDetectionBridgedDataSourceRepositoryMap());
    assertEquals(originBridgedDataSourceRepositoryMap, simSpec.getOriginBridgedDataSourceRepositoryMap());
    assertEquals(waveformBridgedDataSourceRepository, simSpec.getWaveformBridgedDataSourceRepository());
    assertEquals(wftagBridgedDataSourceRepository, simSpec.getWftagBridgedDataSourceRepository());
  }
}