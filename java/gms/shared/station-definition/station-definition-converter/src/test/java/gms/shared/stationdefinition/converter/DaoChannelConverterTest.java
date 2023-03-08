package gms.shared.stationdefinition.converter;

import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelBandType;
import gms.shared.stationdefinition.coi.channel.ChannelDataType;
import gms.shared.stationdefinition.coi.channel.ChannelInstrumentType;
import gms.shared.stationdefinition.coi.channel.ChannelNameUtilities;
import gms.shared.stationdefinition.coi.channel.ChannelOrientationType;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.channel.Orientation;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverterTransform;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.BEAM_COHERENT;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.BRIDGED;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.CHANNEL_GROUP;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.STEERING_AZIMUTH;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.STEERING_SLOWNESS;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_2_LETTER_CHANNEL;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_BDA;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_LDA;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_1;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DaoChannelConverterTest {

  private final FileFrequencyAmplitudePhaseConverter fapConverter = new FileFrequencyAmplitudePhaseConverter();
  private final DaoResponseConverter responseConverter = new DaoResponseConverter();
  private final DaoCalibrationConverter calibrationConverter = new DaoCalibrationConverter();
  private DaoChannelConverter channelConverter;
  private ResponseConverterTransform responseConverterTransform;

  @BeforeEach
  void setUp() {
    channelConverter = new DaoChannelConverter(calibrationConverter, fapConverter);
    responseConverterTransform =
      (wfdiscDao, sensorDao, calibration, frequencyAmplitudePhase) -> responseConverter
        .convertToEntity(wfdiscDao);
  }

  @ParameterizedTest
  @MethodSource("createFullChannelNullCheckSourceWithTransform")
  void testCreateChannelsFromDaosWithTransform_validationChecks(Class<? extends Exception> expectedException,
    SiteChanDao siteChanDao,
    SiteDao siteDao,
    SensorDao sensor,
    InstrumentDao instrument,
    WfdiscDao wfdiscDao,
    ResponseConverterTransform transform) {
    assertThrows(expectedException,
      () -> channelConverter.convert(siteChanDao, siteDao, sensor, instrument, wfdiscDao,
        Range.open(siteChanDao.getId().getOnDate(), siteChanDao.getOffDate()), transform));
  }

  private static Stream<Arguments> createFullChannelNullCheckSourceWithTransform() {
    ResponseConverterTransform transform = (a, b, c, d) -> null;
    return Stream.of(
      Arguments.of(NullPointerException.class,
        null,
        CSSDaoTestFixtures.SITE_DAO_1,
        CSSDaoTestFixtures.SENSOR_DAO_1,
        CSSDaoTestFixtures.INSTRUMENT_DAO_1,
        CSSDaoTestFixtures.WFDISC_DAO_1,
        transform),
      Arguments.of(NullPointerException.class,
        SITE_CHAN_DAO_1,
        null,
        CSSDaoTestFixtures.SENSOR_DAO_1,
        CSSDaoTestFixtures.INSTRUMENT_DAO_1,
        CSSDaoTestFixtures.WFDISC_DAO_1,
        transform),
      Arguments.of(IllegalStateException.class,
        SITE_CHAN_DAO_1,
        CSSDaoTestFixtures.SITE_DAO_1,
        CSSDaoTestFixtures.SENSOR_DAO_1,
        null,
        null,
        transform),
      Arguments.of(NullPointerException.class,
        SITE_CHAN_DAO_1,
        CSSDaoTestFixtures.SITE_DAO_1,
        CSSDaoTestFixtures.SENSOR_DAO_1,
        CSSDaoTestFixtures.INSTRUMENT_DAO_1,
        CSSDaoTestFixtures.WFDISC_DAO_1,
        null)
    );
  }

  @Test
  void testCreateChannelsFromDaosWithTransform() {
    Channel expected = UtilsTestFixtures.getListOfChannelsWithReferenceResponse().stream()
      .filter(channel -> channel.getProcessingMetadata()
        .get(ChannelProcessingMetadataType.CHANNEL_GROUP)
        .equals(SITE_CHAN_DAO_1.getId().getStationCode())).findFirst()
      .orElseThrow();

    Channel result = channelConverter.convert(SITE_CHAN_DAO_1,
      CSSDaoTestFixtures.SITE_DAO_1, CSSDaoTestFixtures.SENSOR_DAO_1,
      CSSDaoTestFixtures.INSTRUMENT_DAO_1, CSSDaoTestFixtures.WFDISC_TEST_DAO_1,
      Range.open(SITE_CHAN_DAO_1.getId().getOnDate(), SITE_CHAN_DAO_1.getOffDate()),
      responseConverterTransform);

    assertEquals(expected, result);
  }

  @Test
  void testCreateChannelsFromDaos_wfdiscSampleRate() {
    Channel expected = Channel.builder()
      .setName("STA.STA01.BHE")
      .setEffectiveAt(SITE_CHAN_DAO_1.getId().getOnDate())
      .setData(Channel.Data.builder()
        .setCanonicalName("STA.STA01.BHE")
        .setEffectiveUntil(SITE_CHAN_DAO_1.getOffDate())
        .setDescription(SITE_CHAN_DAO_1.getChannelDescription())
        .setStation(Station.createVersionReference(SITE_DAO_1.getReferenceStation(), SITE_DAO_1.getId().getOnDate()))
        .setChannelDataType(ChannelDataType.SEISMIC)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.EAST_WEST)
        .setChannelOrientationCode('E')
        .setUnits(Units.NANOMETERS)
        .setNominalSampleRateHz(WFDISC_TEST_DAO_1.getSampRate())
        .setLocation(Location.from(SITE_DAO_1.getLatitude(),
          SITE_DAO_1.getLongitude(),
          SITE_CHAN_DAO_1.getEmplacementDepth(),
          SITE_DAO_1.getElevation()))
        .setOrientationAngles(Orientation.from(SITE_CHAN_DAO_1.getHorizontalAngle(),
          SITE_CHAN_DAO_1.getVerticalAngle()))
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, SITE_DAO_1.getId().getStationCode()))
        .setResponse(Optional.empty())
        .build())
      .build();

    Channel result = channelConverter.convert(SITE_CHAN_DAO_1, SITE_DAO_1,
      CSSDaoTestFixtures.SENSOR_DAO_1,
      null, CSSDaoTestFixtures.WFDISC_TEST_DAO_1,
      Range.open(SITE_CHAN_DAO_1.getId().getOnDate(), SITE_CHAN_DAO_1.getOffDate()),
      responseConverterTransform);

    assertEquals(expected, result);
  }

  @ParameterizedTest
  @MethodSource("createLDAandBDA")
  void testLDAandBDAChannel(Channel expected, SiteChanDao siteChanDao) {

    Channel result = channelConverter.convert(siteChanDao, SITE_DAO_1,
      CSSDaoTestFixtures.SENSOR_DAO_1,
      null, CSSDaoTestFixtures.WFDISC_TEST_DAO_1,
      Range.open(SITE_CHAN_DAO_1.getId().getOnDate(), SITE_CHAN_DAO_1.getOffDate()),
      responseConverterTransform);

    assertEquals(expected, result);
  }

  private static Stream<Arguments> createLDAandBDA() {
    return Stream.of(
      Arguments.of(Channel.builder()
          .setName("STA.STA01.LDA")
          .setEffectiveAt(SITE_CHAN_DAO_1.getId().getOnDate())
          .setData(Channel.Data.builder()
            .setCanonicalName("STA.STA01.LDA")
            .setEffectiveUntil(SITE_CHAN_DAO_1.getOffDate())
            .setDescription(SITE_CHAN_DAO_1.getChannelDescription())
            .setStation(Station.createVersionReference(SITE_DAO_1.getReferenceStation(), SITE_DAO_1.getId().getOnDate()))
            .setChannelDataType(ChannelDataType.INFRASOUND)
            .setChannelBandType(ChannelBandType.LONG_PERIOD)
            .setChannelInstrumentType(ChannelInstrumentType.PRESSURE)
            .setChannelOrientationType(ChannelOrientationType.INFRASOUND)
            .setChannelOrientationCode('F')
            .setUnits(Units.PASCALS)
            .setNominalSampleRateHz(WFDISC_TEST_DAO_1.getSampRate())
            .setLocation(Location.from(SITE_DAO_1.getLatitude(),
              SITE_DAO_1.getLongitude(),
              SITE_CHAN_DAO_1.getEmplacementDepth(),
              SITE_DAO_1.getElevation()))
            .setOrientationAngles(Orientation.from(SITE_CHAN_DAO_1.getHorizontalAngle(),
              SITE_CHAN_DAO_1.getVerticalAngle()))
            .setConfiguredInputs(List.of())
            .setProcessingDefinition(Map.of())
            .setProcessingMetadata(Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, SITE_DAO_1.getId().getStationCode()))
            .setResponse(Optional.empty())
            .build())
          .build(),
        SITE_CHAN_DAO_LDA),
      Arguments.of(Channel.builder()
          .setName("STA.STA01.BDA")
          .setEffectiveAt(SITE_CHAN_DAO_1.getId().getOnDate())
          .setData(Channel.Data.builder()
            .setCanonicalName("STA.STA01.BDA")
            .setEffectiveUntil(SITE_CHAN_DAO_1.getOffDate())
            .setDescription(SITE_CHAN_DAO_1.getChannelDescription())
            .setStation(Station.createVersionReference(SITE_DAO_1.getReferenceStation(), SITE_DAO_1.getId().getOnDate()))
            .setChannelDataType(ChannelDataType.INFRASOUND)
            .setChannelBandType(ChannelBandType.BROADBAND)
            .setChannelInstrumentType(ChannelInstrumentType.PRESSURE)
            .setChannelOrientationType(ChannelOrientationType.INFRASOUND)
            .setChannelOrientationCode('F')
            .setUnits(Units.PASCALS)
            .setNominalSampleRateHz(WFDISC_TEST_DAO_1.getSampRate())
            .setLocation(Location.from(SITE_DAO_1.getLatitude(),
              SITE_DAO_1.getLongitude(),
              SITE_CHAN_DAO_1.getEmplacementDepth(),
              SITE_DAO_1.getElevation()))
            .setOrientationAngles(Orientation.from(SITE_CHAN_DAO_1.getHorizontalAngle(),
              SITE_CHAN_DAO_1.getVerticalAngle()))
            .setConfiguredInputs(List.of())
            .setProcessingDefinition(Map.of())
            .setProcessingMetadata(Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, SITE_DAO_1.getId().getStationCode()))
            .setResponse(Optional.empty())
            .build())
          .build(),
        SITE_CHAN_DAO_BDA)
    );
  }

  @Test
  void testCreateChannelsFromDaos_nullResponse() {
    final Channel channel = assertDoesNotThrow(
      () -> channelConverter.convert(SITE_CHAN_DAO_1, SITE_DAO_1,
        CSSDaoTestFixtures.SENSOR_DAO_1, CSSDaoTestFixtures.INSTRUMENT_DAO_1, null,
        Range.open(SITE_CHAN_DAO_1.getId().getOnDate(), SITE_CHAN_DAO_1.getOffDate()), responseConverterTransform));

    assertNotNull(channel);
    assertEquals(Optional.empty(), channel.getResponse());
  }

  @Test
  void testCreateChannelsFromDaosWithTransform_nullResponse() {
    final Channel channel = assertDoesNotThrow(
      () -> channelConverter.convert(SITE_CHAN_DAO_1,
        CSSDaoTestFixtures.SITE_DAO_1, CSSDaoTestFixtures.SENSOR_DAO_1,
        CSSDaoTestFixtures.INSTRUMENT_DAO_1, null, Range.open(SITE_CHAN_DAO_1.getId().getOnDate(), SITE_CHAN_DAO_1.getOffDate()), responseConverterTransform));

    assertNotNull(channel);
    assertEquals(Optional.empty(), channel.getResponse());
  }

  @ParameterizedTest
  @MethodSource("createFacetedChannelNullCheckSource")
  void testCreateChannelsFromDaos_faceted_nullChecks(SiteChanDao siteChanDao,
    SiteDao siteDao) {
    assertThrows(NullPointerException.class,
      () -> channelConverter.convertToEntityReference(siteDao, siteChanDao));
  }

  private static Stream<Arguments> createFacetedChannelNullCheckSource() {
    return Stream.of(
      Arguments.of(null, CSSDaoTestFixtures.SITE_DAO_1),
      Arguments.of(SITE_CHAN_DAO_1, null)
    );
  }

  @Test
  void testCreateChannelsFromDaos_faceted() {

    Channel testChannel = channelConverter.convertToEntityReference(CSSDaoTestFixtures.SITE_DAO_1, SITE_CHAN_DAO_1);

    final String name = UtilsTestFixtures.getListOfChannelsForDaos().stream()
      .filter(channel -> channel.getProcessingMetadata()
        .get(ChannelProcessingMetadataType.CHANNEL_GROUP)
        .equals(SITE_CHAN_DAO_1.getId().getStationCode())).findFirst()
      .orElseThrow()
      .getName();
    Channel compareChannel = Channel.createEntityReference(name);

    assertEquals(testChannel, compareChannel);
  }

  @ParameterizedTest
  @MethodSource("unitSource")
  void testDetermineUnits(ChannelDataType inputType, Units resultType) {
    final Units result = Units.determineUnits(inputType);
    assertEquals(resultType, result);
  }


  private static Stream<Arguments> unitSource() {
    return Stream.of(
      Arguments.of(ChannelDataType.SEISMIC, Units.NANOMETERS),
      Arguments.of(ChannelDataType.HYDROACOUSTIC, Units.MICROPASCALS),
      Arguments.of(ChannelDataType.INFRASOUND, Units.PASCALS),
      Arguments.of(ChannelDataType.DIAGNOSTIC_SOH, Units.UNITLESS),
      Arguments.of(ChannelDataType.DIAGNOSTIC_WEATHER, Units.UNITLESS),
      Arguments.of(ChannelDataType.WEATHER, Units.UNITLESS)
    );
  }

  @Test
  void testConvertToVersionReferenceFromDaos() {
    Channel expected = UtilsTestFixtures.getListOfChannelsWithResponse().stream()
      .filter(channel -> channel.getProcessingMetadata()
        .get(ChannelProcessingMetadataType.CHANNEL_GROUP)
        .equals(SITE_CHAN_DAO_1.getId().getStationCode())).findFirst()
      .orElseThrow();

    Channel result = channelConverter.convertToVersionReference(SITE_DAO_1, SITE_CHAN_DAO_1);

    assertEquals(
      Channel.createVersionReference(expected.getName(), expected.getEffectiveAt().orElseThrow()),
      result);
  }

  @Test
  void testConvertWith2LetterChannelCode() {
    Channel expected = UtilsTestFixtures.getListOfChannelsWithReferenceResponse().stream()
      .filter(channel -> channel.getProcessingMetadata()
        .get(ChannelProcessingMetadataType.CHANNEL_GROUP)
        .equals(SITE_CHAN_DAO_1.getId().getStationCode())).findFirst()
      .orElseThrow();

    String name = SITE_DAO_1.getReferenceStation()
      .concat(".")
      .concat(SITE_DAO_1.getId().getStationCode())
      .concat(".")
      .concat(SITE_CHAN_2_LETTER_CHANNEL.getId().getChannelCode());
    Channel expected2Letter = expected.toBuilder().setName(name)
      .setData(expected.getData().orElseThrow()
        .toBuilder()
        .setCanonicalName(name)
        .setChannelInstrumentType(ChannelInstrumentType.UNKNOWN)
        .setChannelOrientationType(ChannelOrientationType.VERTICAL)
        .setChannelOrientationCode('Z')
        .build())
      .build();
    Channel result = channelConverter.convert(SITE_CHAN_2_LETTER_CHANNEL,
      CSSDaoTestFixtures.SITE_DAO_1, CSSDaoTestFixtures.SENSOR_DAO_1,
      CSSDaoTestFixtures.INSTRUMENT_DAO_1, CSSDaoTestFixtures.WFDISC_TEST_DAO_1,
      Range.open(SITE_CHAN_2_LETTER_CHANNEL.getId().getOnDate(), SITE_CHAN_2_LETTER_CHANNEL.getOffDate()),
      responseConverterTransform);

    assertEquals(expected2Letter, result);
  }

  @Test
  void testConvertToVersionReferencePreserves2LetterChannelCode() {
    String name = SITE_DAO_1.getReferenceStation()
      .concat(".")
      .concat(SITE_DAO_1.getId().getStationCode())
      .concat(".")
      .concat(SITE_CHAN_2_LETTER_CHANNEL.getId().getChannelCode());

    Channel expected = Channel.createVersionReference(name, SITE_CHAN_2_LETTER_CHANNEL.getId().getOnDate());
    assertEquals(expected, channelConverter.convertToVersionReference(SITE_DAO_1, SITE_CHAN_2_LETTER_CHANNEL));
  }

  @Test
  void testConvertToBeamDerived() {
    double steeringAzimuth = 0.0;
    double steeringSlowness = 0.0;
    String beamChannelGroup = "beam";
    String bridgedMetadata = "/bridged,arid:123";
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = Instant.MAX;
    EnumMap<ChannelProcessingMetadataType, Object> processingMetadataMap = new EnumMap<>(ChannelProcessingMetadataType.class);
    processingMetadataMap.put(BRIDGED, bridgedMetadata);

    BeamDao beam = CSSDaoTestFixtures.createBeamDao(1);

    Channel beamDerived = channelConverter.convertToBeamDerived(SITE_DAO_1,
      SITE_CHAN_DAO_1,
      WFDISC_DAO_1,
      channelEffectiveTime,
      channelEndTime,
      Optional.of(beam),
      processingMetadataMap);

    String channelName = ChannelNameUtilities.createName(beamDerived);

    assertEquals(channelEffectiveTime, beamDerived.getEffectiveAt().orElseThrow());
    assertEquals(channelEndTime, beamDerived.getEffectiveUntil().orElseThrow());
    assertEquals(channelName, beamDerived.getName());
    assertEquals(channelName, beamDerived.getData().orElseThrow().getCanonicalName());

    assertEquals(bridgedMetadata, beamDerived.getProcessingMetadata().get(BRIDGED));
    assertEquals(beamChannelGroup, beamDerived.getProcessingMetadata().get(CHANNEL_GROUP));
    assertEquals(steeringAzimuth, beamDerived.getProcessingMetadata().get(STEERING_AZIMUTH));
    assertEquals(steeringSlowness, beamDerived.getProcessingMetadata().get(STEERING_SLOWNESS));

    assertEquals(SITE_CHAN_DAO_1.getChannelType(), beamDerived.getProcessingDefinition().get(BEAM_COHERENT.name()));
    assertEquals(steeringAzimuth, beamDerived.getProcessingDefinition().get(STEERING_AZIMUTH.name()));
    assertEquals(steeringSlowness, beamDerived.getProcessingDefinition().get(STEERING_SLOWNESS.name()));

    assertEquals(beam.getDescription(), beamDerived.getDescription());
  }
}
