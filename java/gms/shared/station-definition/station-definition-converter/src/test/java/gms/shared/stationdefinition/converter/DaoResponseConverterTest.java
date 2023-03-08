package gms.shared.stationdefinition.converter;

import gms.shared.stationdefinition.coi.channel.Calibration;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_1_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_DAO_END_TIME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DaoResponseConverterTest {

  private DaoResponseConverter daoResponseConverter = new DaoResponseConverter();

  private static WfdiscDao wfdiscDao;
  private static SensorDao sensorDao;
  private static Calibration calibration;
  private static FrequencyAmplitudePhase frequencyAmplitudePhase;
  private static FrequencyAmplitudePhase emptyFrequencyAmplitudePhase;

  private static Response sampleResponse;
  private static Response sampleEntityResponse;

  @BeforeAll
  static void prepareObjects() {
    wfdiscDao = CSSDaoTestFixtures.WFDISC_DAO_1;
    sensorDao = CSSDaoTestFixtures.SENSOR_DAO_1;
    calibration = UtilsTestFixtures.calibration;
    frequencyAmplitudePhase = UtilsTestFixtures.fapResponse;
    emptyFrequencyAmplitudePhase = FrequencyAmplitudePhase.builder()
      .setData(Optional.empty())
      .setId(UUID.randomUUID())
      .build();

    String id = CSSDaoTestFixtures.WFDISC_DAO_1.getStationCode() +
      CSSDaoTestFixtures.WFDISC_DAO_1.getChannelCode();

    sampleResponse = Response.builder()
      .setEffectiveAt(CSSDaoTestFixtures.WFDISC_DAO_1.getTime())
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setData(Response.Data.builder()
        .setFapResponse(UtilsTestFixtures.fapResponse)
        .setCalibration(UtilsTestFixtures.calibration)
        .build())
      .build();

    sampleEntityResponse = Response.builder()
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .build();
  }

  @Test
  void testResponseConverterPass() {

    assertEquals(sampleResponse, daoResponseConverter.convert(
      wfdiscDao, sensorDao, calibration, frequencyAmplitudePhase));
  }

  @Test
  void testResponseConverterPassWithEndTimeFromWfDisc() {
    String id = WFDISC_DAO_END_TIME.getStationCode() + WFDISC_DAO_END_TIME.getChannelCode();
    Response sampleResponseWithEndTime = Response.builder()
      .setEffectiveAt(CSSDaoTestFixtures.WFDISC_DAO_END_TIME.getTime())
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setData(Response.Data.builder()
        .setFapResponse(UtilsTestFixtures.fapResponse)
        .setCalibration(UtilsTestFixtures.calibration)
        .setEffectiveUntil(WFDISC_DAO_END_TIME.getEndTime())
        .build())
      .build();

    assertEquals(sampleResponseWithEndTime, daoResponseConverter.convert(
      WFDISC_DAO_END_TIME, sensorDao, calibration, frequencyAmplitudePhase));
  }

  @Test
  void testResponseConverterPassWithEndTimeFromSensor() {
    String id = WFDISC_DAO_END_TIME.getStationCode() + WFDISC_DAO_END_TIME.getChannelCode();
    Response sampleResponseWithEndTime = Response.builder()
      .setEffectiveAt(CSSDaoTestFixtures.WFDISC_DAO_END_TIME.getTime())
      .setId(UUID.nameUUIDFromBytes(id.getBytes()))
      .setData(Response.Data.builder()
        .setFapResponse(UtilsTestFixtures.fapResponse)
        .setCalibration(UtilsTestFixtures.calibration)
        .setEffectiveUntil(SENSOR_DAO_1_2.getSensorKey().getEndTime())
        .build())
      .build();

    assertEquals(sampleResponseWithEndTime, daoResponseConverter.convert(
      WFDISC_DAO_END_TIME, SENSOR_DAO_1_2, calibration, frequencyAmplitudePhase));
  }

  @Test
  void testEntityResponseConverterPass() {

    assertEquals(sampleEntityResponse, daoResponseConverter.convertToEntity(
      wfdiscDao));
  }

  @Test
  void testResponseConverterPass_withEntityReferenceFeqAmplitudePhase() {
    assertDoesNotThrow(() -> daoResponseConverter.convert(
      wfdiscDao, sensorDao, calibration, emptyFrequencyAmplitudePhase));
  }

  @ParameterizedTest
  @MethodSource("createResponseConverterNullEmptyCheck")
  void testResponseConverter_nullAndEmptyChecks(Class<? extends Exception> exception,
    WfdiscDao wfdiscDao,
    Calibration calibration,
    FrequencyAmplitudePhase frequencyAmplitudePhase) {
    assertThrows(exception,
      () -> daoResponseConverter.convert(wfdiscDao, sensorDao, calibration, frequencyAmplitudePhase));
  }

  private static Stream<Arguments> createResponseConverterNullEmptyCheck() {
    return Stream.of(
      Arguments.of(NullPointerException.class, null, calibration, frequencyAmplitudePhase),
      Arguments.of(NullPointerException.class, wfdiscDao, null, frequencyAmplitudePhase),
      Arguments.of(NullPointerException.class, wfdiscDao, calibration, null)
    );
  }

  @Test
  void testResponseConverterToEntity_nullAndEmptyChecks() {
    assertThrows(NullPointerException.class,
      () -> daoResponseConverter.convertToEntity(null));
  }


}
