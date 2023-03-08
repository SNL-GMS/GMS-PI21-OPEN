package gms.shared.stationdefinition.repository;

import gms.shared.stationdefinition.coi.channel.Calibration;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.converter.DaoResponseConverter;
import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.converter.util.assemblers.ResponseAssembler;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestSensorDaos;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestWfdiscDaos;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.INSTID_1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.getResponse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedResponseRepositoryTest {

  @Mock
  private WfdiscDatabaseConnector wfdiscDatabaseConnector;

  @Mock
  private SensorDatabaseConnector sensorDatabaseConnector;

  @Mock
  private InstrumentDatabaseConnector instrumentDatabaseConnector;

  @Mock
  private StationDefinitionIdUtility stationDefinitionIdUtility;

  @Mock
  private ResponseAssembler responseAssembler;

  private BridgedResponseRepository responseRepository;

  @BeforeEach
  void setup() {
    responseRepository = new BridgedResponseRepository(wfdiscDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      stationDefinitionIdUtility,
      responseAssembler);
  }

  @ParameterizedTest
  @MethodSource("getFindResponsesByIdArguments")
  void testFindResponsesByIdValidation(List<UUID> responseIds, Instant effectiveTime) {
    assertThrows(NullPointerException.class,
      () -> responseRepository.findResponsesById(responseIds, effectiveTime));
  }

  static Stream<Arguments> getFindResponsesByIdArguments() {
    return Stream.of(arguments(null, Instant.EPOCH),
      arguments(List.of(UUID.randomUUID()), null));
  }

  @Test
  void testFindResponsesById() {
    when(stationDefinitionIdUtility.getChannelForResponseId(RESPONSE_1.getId()))
      .thenReturn(Optional.of("STA.CHAN.CHAN01"));
    when(wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(any(), eq(Instant.EPOCH), any())).thenReturn(List.of(WFDISC_DAO_1));
    when(sensorDatabaseConnector.findSensorVersionsByNameAndTime(any(), eq(Instant.EPOCH))).thenReturn(List.of(SENSOR_DAO_1));
    when(instrumentDatabaseConnector.findInstruments((any()))).thenReturn(List.of(INSTRUMENT_DAO_1));
    when(responseAssembler.buildAllForTime(eq(Instant.EPOCH),
      any(), any(), any(), eq(Optional.empty())))
      .thenReturn(List.of(RESPONSE_1));

    List<Response> actual = responseRepository.findResponsesById(List.of(RESPONSE_1.getId()), Instant.EPOCH);
    assertEquals(List.of(RESPONSE_1), actual);
    verify(stationDefinitionIdUtility).getChannelForResponseId(RESPONSE_1.getId());
    verify(wfdiscDatabaseConnector).findWfdiscsByNameAndTimeRange(any(), eq(Instant.EPOCH), any());
    verify(sensorDatabaseConnector).findSensorVersionsByNameAndTime(any(), eq(Instant.EPOCH));
    verify(instrumentDatabaseConnector).findInstruments(any());
    verify(responseAssembler).buildAllForTime(eq(Instant.EPOCH),
      any(), any(), any(),
      eq(Optional.empty()));
    verifyNoMoreInteractions(stationDefinitionIdUtility,
      wfdiscDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      responseAssembler);
  }

  @ParameterizedTest
  @MethodSource("getFindResponsesByIdAndTimeRangeArguments")
  void tesFindResponsesByIdAndTimeRangeValidation(Class<? extends Exception> expectedException,
    Collection<UUID> responseIds,
    Instant startTime,
    Instant endTime) {

    assertThrows(expectedException,
      () -> responseRepository.findResponsesByIdAndTimeRange(responseIds, startTime, endTime));
  }

  static Stream<Arguments> getFindResponsesByIdAndTimeRangeArguments() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    return Stream.of(arguments(NullPointerException.class, null, startTime, endTime),
      arguments(NullPointerException.class, List.of(UUID.randomUUID()), null, endTime),
      arguments(NullPointerException.class, List.of(UUID.randomUUID()), startTime, null),
      arguments(IllegalStateException.class, List.of(UUID.randomUUID()), endTime, startTime));
  }

  @Test
  void testFindResponsesByIdAndTimeRange() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);

    List<WfdiscDao> wfdiscs = List.of(WFDISC_DAO_1);
    List<SensorDao> sensors = List.of(SENSOR_DAO_1);
    List<InstrumentDao> instruments = List.of(INSTRUMENT_DAO_1);
    List<Response> expected = List.of(RESPONSE_1);
    when(stationDefinitionIdUtility.getChannelForResponseId(RESPONSE_1.getId()))
      .thenReturn(Optional.of("STA.CHAN.CHAN01"));
    when(wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(any(), eq(startTime), any()))
      .thenReturn(wfdiscs);
    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(any(), eq(startTime), eq(endTime)))
      .thenReturn(sensors);
    when(instrumentDatabaseConnector.findInstruments(List.of(INSTID_1)))
      .thenReturn(instruments);
    when(responseAssembler.buildAllForTimeRange(eq(startTime), eq(endTime),
      any(), any(), any(), eq(Optional.empty())))
      .thenReturn(expected);

    List<Response> actual = responseRepository.findResponsesByIdAndTimeRange(List.of(RESPONSE_1.getId()),
      startTime,
      endTime);

    assertEquals(expected, actual);
    verify(stationDefinitionIdUtility).getChannelForResponseId(RESPONSE_1.getId());
    verify(wfdiscDatabaseConnector).findWfdiscsByNameAndTimeRange(any(), eq(startTime), any());
    verify(sensorDatabaseConnector).findSensorsByKeyAndTimeRange(any(), eq(startTime), eq(endTime));
    verify(instrumentDatabaseConnector).findInstruments(List.of(INSTID_1));
    verify(responseAssembler).buildAllForTimeRange(eq(startTime), eq(endTime),
      any(), any(), any(), eq(Optional.empty()));
    verifyNoMoreInteractions(wfdiscDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      responseAssembler);
  }

  @Test
  void testLoadResponseFromWfdiscChannelNameFound() {
    var wfid = WFDISC_DAO_1.getId();
    var channelName = "STA.CHAN.CHAN01";
    var startTime = WFDISC_DAO_1.getTime();
    var endTime = WFDISC_DAO_1.getEndTime();
    var siteChanKey = new SiteChanKey(WFDISC_DAO_1.getStationCode(), WFDISC_DAO_1.getChannelCode(), startTime);
    var instruments = List.of(INSTRUMENT_DAO_1);
    var sensors = List.of(SENSOR_DAO_1);
    var wfdiscs = List.of(WFDISC_DAO_1);
    var instrumentIds = sensors.stream()
      .map(SensorDao::getInstrument)
      .map(InstrumentDao::getInstrumentId)
      .collect(Collectors.toList());
    double[] frequencies = {2.0E-4, 2.04668E-4, 2.09445E-4, 2.143335E-4, 2.193361E-4, 2.244555E-4, 2.296943E-4, 2.350554E-4, 2.405417E-4, 2.46156E-4, 2.519013E-4, 2.577807E-4, 2.637974E-4, 2.699545E-4, 2.762553E-4, 2.827031E-4, 2.893015E-4, 2.960538E-4, 3.029638E-4, 3.10035E-4};
    double[] amplitudes = {2.775677E-5, 2.974503E-5, 3.187562E-5, 3.415871E-5, 3.660521E-5, 3.922679E-5, 4.203595E-5, 4.504608E-5, 4.827154E-5, 5.172769E-5, 5.543098E-5, 5.939903E-5, 6.36507E-5, 6.82062E-5, 7.308716E-5, 7.831672E-5, 8.391966E-5, 8.992252E-5, 9.635367E-5, 1.032435E-4};
    double[] ampStdDev = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    double[] phase = {-1.786542, -1.791653, -1.796888, -1.802251, -1.807744, -1.813373, -1.81914, -1.825049, -1.831103, -1.837307, -1.843665, -1.850181, -1.856859, -1.863703, -1.870719, -1.877911, -1.885284, -1.892843, -1.900594, -1.908541};
    double[] phaseStdDev = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    var fapFileLocation = "file.fap";
    var expectedFap = FrequencyAmplitudePhase.builder()
      .setData(FrequencyAmplitudePhase.Data.builder()
        .setAmplitudeResponseUnits(Units.COUNTS_PER_NANOMETER)
        .setAmplitudeResponse(amplitudes)
        .setAmplitudeResponseStdDev(ampStdDev)
        .setPhaseResponse(phase)
        .setPhaseResponseStdDev(phaseStdDev)
        .setPhaseResponseUnits(Units.DEGREES)
        .setFrequencies(frequencies)
        .build())
      .setId(UUID.nameUUIDFromBytes(fapFileLocation.getBytes()))
      .build();
    var expectedCalibration = Calibration.from(WFDISC_DAO_1.getCalper(),
      Duration.ZERO,
      DoubleValue.from(WFDISC_DAO_1.getCalib(),
        Optional.empty(), Units.UNITLESS));
    var expectedResponse = Response.builder().setData(Response.Data.builder()
        .setFapResponse(expectedFap)
        .setCalibration(expectedCalibration)
        .setEffectiveUntil(RESPONSE_1.getEffectiveUntil())
        .build())
      .setId(RESPONSE_1.getId())
      .setEffectiveAt(RESPONSE_1.getEffectiveAt())
      .build();
    when(stationDefinitionIdUtility.getChannelForResponseId(any()))
      .thenReturn(Optional.of(channelName));
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(wfid))).thenReturn(wfdiscs);
    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime)).thenReturn(sensors);
    when(instrumentDatabaseConnector.findInstruments(instrumentIds)).thenReturn(instruments);
    when(responseAssembler.buildResponseEntity(WFDISC_DAO_1)).thenReturn(new DaoResponseConverter().convertToEntity(WFDISC_DAO_1));
    when(responseAssembler.buildAllForTimeRange(startTime,
      endTime,
      wfdiscs,
      sensors,
      instruments,
      Optional.of(channelName)))
      .thenReturn(List.of(expectedResponse));
    var actual = responseRepository.loadResponseFromWfdisc(wfid);
    var actualCalibration = actual.getCalibration();
    assertEquals(expectedResponse, actual);
    assertEquals(expectedCalibration, actualCalibration);
    verify(stationDefinitionIdUtility, times(1)).getChannelForResponseId(any());
    verify(stationDefinitionIdUtility, times(1)).storeWfidResponseMapping(wfid, expectedResponse);
    verify(wfdiscDatabaseConnector, times(1)).findWfdiscsByWfids(List.of(wfid));
    verify(sensorDatabaseConnector, times(1)).findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime);
    verify(instrumentDatabaseConnector, times(1)).findInstruments(instrumentIds);
    verify(responseAssembler, times(1)).buildResponseEntity(WFDISC_DAO_1);
    verify(responseAssembler, times(1))
      .buildAllForTimeRange(startTime,
        endTime,
        wfdiscs,
        sensors,
        instruments,
        Optional.of(channelName));
    verifyNoMoreInteractions(stationDefinitionIdUtility,
      wfdiscDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      responseAssembler);
  }

  @Test
  void testLoadResponseFromWfdiscNoChannelNameFound() {
    var wfid = WFDISC_DAO_1.getId();
    var startTime = WFDISC_DAO_1.getTime();
    var endTime = WFDISC_DAO_1.getEndTime();
    var siteChanKey = new SiteChanKey(WFDISC_DAO_1.getStationCode(), WFDISC_DAO_1.getChannelCode(), startTime);
    var instruments = List.of(INSTRUMENT_DAO_1);
    var sensors = List.of(SENSOR_DAO_1);
    var wfdiscs = List.of(WFDISC_DAO_1);
    var instrumentIds = sensors.stream()
      .map(SensorDao::getInstrument)
      .map(InstrumentDao::getInstrumentId)
      .collect(Collectors.toList());
    when(stationDefinitionIdUtility.getChannelForResponseId(any()))
      .thenReturn(Optional.empty());
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(wfid))).thenReturn(wfdiscs);
    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime)).thenReturn(sensors);
    when(instrumentDatabaseConnector.findInstruments(instrumentIds)).thenReturn(instruments);
    when(responseAssembler.buildResponseEntity(WFDISC_DAO_1)).thenReturn(new DaoResponseConverter().convertToEntity(WFDISC_DAO_1));
    when(responseAssembler.buildAllForTimeRange(startTime,
      endTime,
      wfdiscs,
      sensors,
      instruments,
      Optional.empty()))
      .thenReturn(List.of(RESPONSE_1));
    var expectedCalibration = Calibration.from(WFDISC_DAO_1.getCalper(),
      Duration.ZERO,
      DoubleValue.from(WFDISC_DAO_1.getCalib(),
        Optional.empty(), Units.UNITLESS));
    var expectedResponse = Response.builder().setData(Response.Data.builder()
        .setFapResponse(RESPONSE_1.getFapResponse())
        .setCalibration(expectedCalibration)
        .setEffectiveUntil(RESPONSE_1.getEffectiveUntil())
        .build())
      .setId(RESPONSE_1.getId())
      .setEffectiveAt(RESPONSE_1.getEffectiveAt())
      .build();
    var actual = responseRepository.loadResponseFromWfdisc(wfid);
    var actualCalibration = actual.getCalibration();
    assertEquals(expectedResponse, actual);
    assertEquals(expectedCalibration, actualCalibration);
    verify(stationDefinitionIdUtility, times(1)).getChannelForResponseId(any());
    verify(stationDefinitionIdUtility, times(1)).storeWfidResponseMapping(wfid, expectedResponse);
    verify(wfdiscDatabaseConnector, times(1)).findWfdiscsByWfids(List.of(wfid));
    verify(sensorDatabaseConnector, times(1)).findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime);
    verify(instrumentDatabaseConnector, times(1)).findInstruments(instrumentIds);
    verify(responseAssembler, times(1)).buildResponseEntity(WFDISC_DAO_1);
    verify(responseAssembler, times(1))
      .buildAllForTimeRange(startTime,
        endTime,
        wfdiscs,
        sensors,
        instruments,
        Optional.empty());
    verifyNoMoreInteractions(stationDefinitionIdUtility,
      wfdiscDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      responseAssembler);
  }

  @Test
  void testLoadEntityReferenceResponseFromWfdisc() {
    var wfid = WFDISC_DAO_1.getId();
    var startTime = WFDISC_DAO_1.getTime();
    var endTime = WFDISC_DAO_1.getEndTime();
    var siteChanKey = new SiteChanKey(WFDISC_DAO_1.getStationCode(), WFDISC_DAO_1.getChannelCode(), startTime);
    var instruments = List.of(INSTRUMENT_DAO_1);
    var sensors = List.of(SENSOR_DAO_1);
    var wfdiscs = List.of(WFDISC_DAO_1);
    var instrumentIds = sensors.stream()
      .map(SensorDao::getInstrument)
      .map(InstrumentDao::getInstrumentId)
      .collect(Collectors.toList());
    when(stationDefinitionIdUtility.getChannelForResponseId(any()))
      .thenReturn(Optional.empty());
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(wfid))).thenReturn(wfdiscs);
    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime)).thenReturn(sensors);
    when(instrumentDatabaseConnector.findInstruments(instrumentIds)).thenReturn(instruments);
    when(responseAssembler.buildResponseEntity(WFDISC_DAO_1)).thenReturn(new DaoResponseConverter().convertToEntity(WFDISC_DAO_1));
    when(responseAssembler.buildAllForTimeRange(startTime,
      endTime,
      wfdiscs,
      sensors,
      instruments,
      Optional.empty()))
      .thenReturn(List.of());
    var id = WFDISC_DAO_1.getStationCode() + WFDISC_DAO_1.getChannelCode();
    var expectedResponse = Response.createEntityReference(UUID.nameUUIDFromBytes(id.getBytes()));
    var actual = responseRepository.loadResponseFromWfdisc(wfid);
    assertEquals(expectedResponse, actual);
    verify(stationDefinitionIdUtility, times(1)).getChannelForResponseId(any());
    verify(stationDefinitionIdUtility, times(1)).storeWfidResponseMapping(wfid, expectedResponse);
    verify(wfdiscDatabaseConnector, times(1)).findWfdiscsByWfids(List.of(wfid));
    verify(sensorDatabaseConnector, times(1)).findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime);
    verify(instrumentDatabaseConnector, times(1)).findInstruments(instrumentIds);
    verify(responseAssembler, times(1)).buildResponseEntity(WFDISC_DAO_1);
    verify(responseAssembler, times(1))
      .buildAllForTimeRange(startTime,
        endTime,
        wfdiscs,
        sensors,
        instruments,
        Optional.empty());
    verifyNoMoreInteractions(stationDefinitionIdUtility,
      wfdiscDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      responseAssembler);
  }

  @Test
  void testLoadResponseFromWfdiscNoWfdiscFound() {
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(any())).thenReturn(List.of());

    Assertions.assertThrows(IllegalStateException.class,
      () -> responseRepository.loadResponseFromWfdisc(1L));
  }

  @Test
  void testFindResponsesGivenSensorAndWfdisc() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plus(Duration.ofSeconds(5));

    var stationDefinitionDataHolder =
      new StationDefinitionDataHolder(null, null, getTestSensorDaos(), null, getTestWfdiscDaos());
    var instrumentList = getTestSensorDaos().stream()
        .map(SensorDao::getInstrument)
          .collect(Collectors.toList());
    var inids = instrumentList.stream()
      .map(InstrumentDao::getInstrumentId)
        .collect(Collectors.toList());
    var responses = List.of(getResponse("a test name"));

    when(instrumentDatabaseConnector.findInstruments(inids)).thenReturn(instrumentList);
    when(responseAssembler.buildAllForTimeRange(startTime, endTime, getTestWfdiscDaos(), getTestSensorDaos(), instrumentList, Optional.empty()))
      .thenReturn(responses);

    final var result = responseRepository.findResponsesGivenSensorAndWfdisc(stationDefinitionDataHolder,
      startTime, endTime);

    assertNotNull(result);
    Assert.assertEquals(responses.size(), result.size());
    verify(instrumentDatabaseConnector, times(1)).findInstruments(inids);
  }
}