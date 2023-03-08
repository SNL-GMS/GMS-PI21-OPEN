package gms.shared.stationdefinition.converter.util.assemblers;

import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.converter.DaoCalibrationConverter;
import gms.shared.stationdefinition.converter.DaoResponseConverter;
import gms.shared.stationdefinition.converter.FileFrequencyAmplitudePhaseConverter;
import gms.shared.stationdefinition.converter.interfaces.CalibrationConverter;
import gms.shared.stationdefinition.converter.interfaces.FrequencyAmplitudePhaseConverter;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverter;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SENSOR_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.END_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.START_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ResponseAssemblerTest {

  private ResponseConverter responseConverter;
  private CalibrationConverter calibrationConverter;
  private FrequencyAmplitudePhaseConverter frequencyAmplitudePhaseConverter;

  private ResponseAssembler responseAssembler;

  @BeforeEach
  void setup() {
    responseConverter = new DaoResponseConverter();
    calibrationConverter = new DaoCalibrationConverter();
    frequencyAmplitudePhaseConverter = new FileFrequencyAmplitudePhaseConverter();
    responseAssembler = new ResponseAssembler(responseConverter,
      calibrationConverter,
      frequencyAmplitudePhaseConverter);
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeValidationArguments")
  void testBuildAllForTimeValidation(Instant effectiveAt,
    List<WfdiscDao> wfdiscs,
    List<SensorDao> sensors,
    List<InstrumentDao> instrumentDaos,
    Optional<String> channelName) {

    assertThrows(NullPointerException.class,
      () -> responseAssembler.buildAllForTime(effectiveAt, wfdiscs, sensors, instrumentDaos, channelName));
  }

  static Stream<Arguments> getBuildAllForTimeValidationArguments() {
    Optional<String> emptyChannelName = Optional.empty();
    return Stream.of(arguments(null, List.of(WFDISC_TEST_DAO_1), List.of(SENSOR_DAO_1), List.of(INSTRUMENT_DAO_1), emptyChannelName),
      arguments(Instant.EPOCH, null, List.of(SENSOR_DAO_1), List.of(INSTRUMENT_DAO_1), emptyChannelName),
      arguments(Instant.EPOCH, List.of(WFDISC_TEST_DAO_1), null, List.of(INSTRUMENT_DAO_1), emptyChannelName),
      arguments(Instant.EPOCH, List.of(WFDISC_TEST_DAO_1), List.of(SENSOR_DAO_1), null, emptyChannelName));
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeArguments")
  void testBuildAllForTime(List<Response> expected,
    Instant effectiveAt,
    List<WfdiscDao> wfdiscs,
    List<SensorDao> sensors,
    List<InstrumentDao> instruments,
    Optional<String> channelName) {

    List<Response> actual = responseAssembler.buildAllForTime(effectiveAt, wfdiscs, sensors, instruments, channelName);
    assertEquals(expected, actual);
  }

  static Stream<Arguments> getBuildAllForTimeArguments() {
    Optional<String> emptyChannelName = Optional.empty();
    return Stream.of(arguments(List.of(), Instant.EPOCH, List.of(), List.of(), List.of(), emptyChannelName),
      arguments(List.of(), Instant.EPOCH, List.of(WFDISC_TEST_DAO_1), List.of(SENSOR_DAO_1), List.of(INSTRUMENT_DAO_1), emptyChannelName),
      arguments(List.of(), ONDATE, List.of(WFDISC_TEST_DAO_1), List.of(SENSOR_DAO_2), List.of(INSTRUMENT_DAO_1), emptyChannelName),
      arguments(List.of(), ONDATE, List.of(WFDISC_TEST_DAO_1), List.of(SENSOR_DAO_1), List.of(INSTRUMENT_DAO_2), emptyChannelName),
      arguments(List.of(new DaoResponseConverter().convert(WFDISC_TEST_DAO_1, SENSOR_DAO_1,
          new DaoCalibrationConverter().convert(WFDISC_TEST_DAO_1, SENSOR_DAO_1),
          new FileFrequencyAmplitudePhaseConverter().convertToEntityReference(Path.of(INSTRUMENT_DAO_1.getDirectory(),
            INSTRUMENT_DAO_1.getDataFile()).toString()))),
        ONDATE, List.of(WFDISC_TEST_DAO_1), List.of(SENSOR_DAO_1), List.of(INSTRUMENT_DAO_1), emptyChannelName));
  }

  @ParameterizedTest
  @MethodSource("getBuildAllForTimeRangeValidationArguments")
  void testBuildAllForTimeRangeValidation(Class<? extends Exception> expectedException,
    Instant startTime,
    Instant endTime,
    List<WfdiscDao> wfdiscs,
    List<SensorDao> sensors,
    List<InstrumentDao> instruments,
    Optional<String> channelName) {

    assertThrows(expectedException,
      () -> responseAssembler.buildAllForTimeRange(startTime, endTime, wfdiscs, sensors, instruments, channelName));
  }

  static Stream<Arguments> getBuildAllForTimeRangeValidationArguments() {
    Optional<String> emptyChannelName = Optional.empty();
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    return Stream.of(
      arguments(NullPointerException.class,
        null,
        endTime,
        List.of(WFDISC_TEST_DAO_1),
        List.of(SENSOR_DAO_1),
        List.of(INSTRUMENT_DAO_1),
        emptyChannelName),
      arguments(NullPointerException.class,
        startTime,
        null,
        List.of(WFDISC_TEST_DAO_1),
        List.of(SENSOR_DAO_1),
        List.of(INSTRUMENT_DAO_1),
        emptyChannelName),
      arguments(NullPointerException.class,
        startTime,
        endTime,
        null,
        List.of(SENSOR_DAO_1),
        List.of(INSTRUMENT_DAO_1),
        emptyChannelName),
      arguments(NullPointerException.class,
        startTime,
        endTime,
        List.of(WFDISC_TEST_DAO_1),
        null,
        List.of(INSTRUMENT_DAO_1),
        emptyChannelName),
      arguments(NullPointerException.class,
        startTime,
        endTime,
        List.of(WFDISC_TEST_DAO_1),
        List.of(SENSOR_DAO_1),
        null,
        emptyChannelName),
      arguments(IllegalStateException.class,
        endTime,
        startTime,
        List.of(WFDISC_TEST_DAO_1),
        List.of(SENSOR_DAO_1),
        List.of(INSTRUMENT_DAO_1),
        emptyChannelName));
  }

  @Test
  void testBuildAllForTimeRange() {
    List<Response> responses = responseAssembler.buildAllForTimeRange(START_TIME,
      END_TIME,
      List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_4),
      List.of(SENSOR_DAO_1),
      List.of(INSTRUMENT_DAO_1),
      Optional.empty());

    assertNotNull(responses);
    assertEquals(2, responses.size());
  }

  @Test
  void testBuildResponseEntity() {
    var id = WFDISC_DAO_1.getStationCode() + WFDISC_DAO_1.getChannelCode();
    var expected = Response.createEntityReference(UUID.nameUUIDFromBytes(id.getBytes()));

    assertEquals(responseAssembler.buildResponseEntity(WFDISC_DAO_1), expected);
  }

}