package gms.testtools.simulators.bridgeddatasourcesimulator.application;


import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.Site;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.SiteChan;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus;
import gms.testtools.simulators.bridgeddatasourcestationsimulator.BridgedDataSourceStationSimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedDataSourceSimulatorControllerTest {

  private static final String CURRENT_STATE_IS_UNINITIALIZED_ERROR = "Invalid Status Transition Detected. The current status is UNINITIALIZED. The next valid status transitions are [INITIALIZE].";
  private static final String CURRENT_STATE_IS_INITIALIZED_ERROR = "Invalid Status Transition Detected. The current status is INITIALIZED. The next valid status transitions are [START, CLEANUP].";
  private static final String CURRENT_STATE_IS_STARTED_ERROR = "Invalid Status Transition Detected. The current status is STARTED. The next valid status transitions are [STOP].";
  private static final String CURRENT_STATE_IS_STOPPED_ERROR = "Invalid Status Transition Detected. The current status is STOPPED. The next valid status transitions are [START, CLEANUP].";
  public static final String PLACEHOLDER = "placeholder";
  //This spec uses the archi guidance defaults = 45 days of operational time period and 4 days calib update.
  private final BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec = BridgedDataSourceSimulatorSpec.builder()
    .setSeedDataStartTime(Instant.parse("2010-05-20T16:00:00.00Z"))
    .setSeedDataEndTime(Instant.parse("2010-05-20T18:00:00.00Z"))
    .setSimulationStartTime(Instant.now())
    .setOperationalTimePeriod(Duration.ofHours(1080))
    .setCalibUpdateFrequency(Duration.ofHours(96))
    .build();

  private BridgedDataSourceSimulatorController simulatorController;

  @Mock
  private BridgedDataSourceSimulatorStateMachine stateMachine;

  @Mock
  private BridgedDataSourceDataSimulator simulator1;
  @Mock
  private BridgedDataSourceDataSimulator simulator2;
  @Mock
  private BridgedDataSourceStationSimulator simulator3;

  private List<BridgedDataSourceDataSimulator> dataSimulators;

  @BeforeEach
  void testSetup() {
    dataSimulators = List.of(simulator1, simulator2, simulator3);
    simulatorController = BridgedDataSourceSimulatorController.create(stateMachine, dataSimulators);
  }

  @ParameterizedTest
  @MethodSource("constructorValidationCases")
  void testCreateValidation(BridgedDataSourceSimulatorStateMachine stateMachine,
    List<BridgedDataSourceDataSimulator> dataSimulators,
    Class<? extends Exception> expectedError) {
    assertThrows(expectedError, () -> BridgedDataSourceSimulatorController.create(stateMachine, dataSimulators));
  }

  private static Stream<Arguments> constructorValidationCases() {
    List<BridgedDataSourceDataSimulator> nullSimulatorList = null;
    List<BridgedDataSourceDataSimulator> emptySimulatorList = List.of();
    BridgedDataSourceDataSimulator nullSimulator = null;
    final ArrayList<BridgedDataSourceDataSimulator> bridgedDataSourceDataSimulators = new ArrayList<>();
    bridgedDataSourceDataSimulators.add(nullSimulator);
    BridgedDataSourceSimulatorStateMachine mockStateMachine = mock(BridgedDataSourceSimulatorStateMachine.class);

    return Stream.of(
      arguments(null, bridgedDataSourceDataSimulators, NullPointerException.class),
      arguments(mockStateMachine, nullSimulatorList, NullPointerException.class),
      arguments(mockStateMachine, emptySimulatorList, IllegalArgumentException.class),
      arguments(mockStateMachine, bridgedDataSourceDataSimulators, IllegalArgumentException.class)
    );
  }

  @Test
  void testInitialize() {
    assertDoesNotThrow(() -> simulatorController.initialize(bridgedDataSourceSimulatorSpec));
    verify(stateMachine).verifyInitializeTransition(bridgedDataSourceSimulatorSpec);
  }

  @Test
  void testStart() {
    assertDoesNotThrow(() -> simulatorController.start(PLACEHOLDER));
    verify(stateMachine).verifyStartTransition();
    verify(stateMachine).start(PLACEHOLDER);
    dataSimulators.forEach(simulator -> verify(simulator).start(PLACEHOLDER));
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  @Test
  void testStop() {
    assertDoesNotThrow(() -> simulatorController.stop(PLACEHOLDER));
    verify(stateMachine).verifyStopTransition();
    verify(stateMachine).stop(PLACEHOLDER);
    dataSimulators.forEach(simulator -> verify(simulator).stop(PLACEHOLDER));
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  @Test
  void testCleanup() {
    assertDoesNotThrow(() -> simulatorController.cleanup(PLACEHOLDER));
    verify(stateMachine).verifyCleanupTransition();
    verify(stateMachine).cleanup(PLACEHOLDER);
    dataSimulators.forEach(simulator -> verify(simulator).cleanup(PLACEHOLDER));
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  @Test
  void testGetAsyncSimulatorFlux() {
    ErrorCapturingFunction function =
      ErrorCapturingFunction.create(simulator -> simulator.initialize(bridgedDataSourceSimulatorSpec));
    StepVerifier.create(simulatorController.getAsyncSimulatorFlux(function))
      .expectNextCount(3)
      .verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("getStoreSiteVersionArguments")
  void testStoreNewSiteVersionsValidation(Consumer<BridgedDataSourceSimulatorStateMachine> mockSetup,
    Class<? extends Exception> expectedException,
    String expectedMessage,
    List<Site> sites) {

    mockSetup.accept(stateMachine);
    Exception exception = assertThrows(expectedException, () -> simulatorController.storeNewSiteVersions(sites));
    assertEquals(expectedMessage, exception.getMessage());
    verify(stateMachine).status("status");
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  static Stream<Arguments> getStoreSiteVersionArguments() {
    Consumer<BridgedDataSourceSimulatorStateMachine> invalidStateSetup = stateMachine ->
      when(stateMachine.status("status")).thenReturn(BridgedDataSourceSimulatorStatus.UNINITIALIZED);
    Consumer<BridgedDataSourceSimulatorStateMachine> validStatusSetup = stateMachine ->
      when(stateMachine.status("status")).thenReturn(BridgedDataSourceSimulatorStatus.INITIALIZED);

    return Stream.of(
      arguments(invalidStateSetup,
        IllegalStateException.class,
        "Cannot store new site versions if simulator is uninitialized.",
        getSiteList()),
      arguments(validStatusSetup,
        NullPointerException.class,
        "Cannot store null sites",
        null),
      arguments(validStatusSetup,
        IllegalArgumentException.class,
        "Cannot store empty sites",
        List.of()));
  }

  @Test
  void testStoreNewSiteVersions() {
    when(stateMachine.status("status")).thenReturn(BridgedDataSourceSimulatorStatus.INITIALIZED);
    List<Site> sites = getSiteList();
    simulatorController.storeNewSiteVersions(sites);
    verify(stateMachine).status("status");
    verify(simulator3).storeNewSiteVersions(sites);
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  private static List<Site> getSiteList() {
    return List.of(Site.builder()
      .setStationCode("WAH")
      .setOnDate(Instant.now())
      .setOffDate(Instant.now())
      .setLatitude(1)
      .setLongitude(2)
      .setElevation(3)
      .setStationName("WOH")
      .setStationType(StaType.SINGLE_STATION)
      .setDegreesNorth(4)
      .setDegreesEast(5)
      .setReferenceStation("REF")
      .setLoadDate(Instant.now()).build());
  }

  @ParameterizedTest
  @MethodSource("getStoreChannelVersionArguments")
  void testStoreChannelVersionsValidation(Consumer<BridgedDataSourceSimulatorStateMachine> mockSetup,
    Class<? extends Exception> expectedException,
    String expectedMessage,
    List<SiteChan> siteChans) {

    mockSetup.accept(stateMachine);
    Exception exception = assertThrows(expectedException, () -> simulatorController.storeNewChannelVersions(siteChans));
    assertEquals(expectedMessage, exception.getMessage());
    verify(stateMachine).status("status");
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  static Stream<Arguments> getStoreChannelVersionArguments() {
    Consumer<BridgedDataSourceSimulatorStateMachine> invalidStateSetup = stateMachine ->
      when(stateMachine.status("status")).thenReturn(BridgedDataSourceSimulatorStatus.UNINITIALIZED);
    Consumer<BridgedDataSourceSimulatorStateMachine> validStatusSetup = stateMachine ->
      when(stateMachine.status("status")).thenReturn(BridgedDataSourceSimulatorStatus.INITIALIZED);

    return Stream.of(
      arguments(invalidStateSetup,
        IllegalStateException.class,
        "Cannot store new channel versions if simulator is uninitialized.",
        getSiteList()),
      arguments(validStatusSetup,
        NullPointerException.class,
        "Cannot store null channel versions",
        null),
      arguments(validStatusSetup,
        IllegalArgumentException.class,
        "Cannot store empty channel versions",
        List.of()));
  }

  @Test
  void testStoreNewChannelVersions() {
    when(stateMachine.status("status")).thenReturn(BridgedDataSourceSimulatorStatus.INITIALIZED);
    List<SiteChan> siteChans = getSiteChanList();
    simulatorController.storeNewChannelVersions(siteChans);
    verify(stateMachine).status("status");
    verify(simulator3).storeNewChannelVersions(siteChans);
    verifyNoMoreInteractions(stateMachine, simulator1, simulator2, simulator3);
  }

  private static List<SiteChan> getSiteChanList() {
    return List.of(SiteChan.builder()
      .setStationCode("STA1")
      .setChannelCode("CHAN1")
      .setOnDate(Instant.now())
      .setOffDate(Instant.now())
      .setChannelType(ChannelType.N)
      .setChannelDescription("STRING")
      .setEmplacementDepth(1)
      .setHorizontalAngle(2)
      .setVerticalAngle(3)
      .setLoadDate(Instant.now())
      .build());
  }

}