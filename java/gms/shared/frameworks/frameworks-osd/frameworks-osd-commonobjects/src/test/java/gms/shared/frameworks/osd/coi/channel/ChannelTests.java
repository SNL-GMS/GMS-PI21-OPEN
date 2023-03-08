package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.signaldetection.FilterCausality;
import gms.shared.frameworks.osd.coi.signaldetection.FilterDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.GROUP_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.filterDefinition;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.filtered;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.raw;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.referenceChannel;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChannelTests {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(raw, Channel.class);
  }

  @Test
  void testEmptyNameThrowsException() {
    final Exception exception = assertThrows(Exception.class,
      () -> raw.toBuilder().setName("").build());
    assertEquals("name should not be an empty field", exception.getMessage());
  }

  @Test
  void testEmptyConfiguredInputThrowsException() {
    final Exception exception = assertThrows(Exception.class,
      () -> raw.toBuilder().setConfiguredInputs(List.of("", "chan")).build());
    assertEquals("none of the configured inputs should be an empty field", exception.getMessage());
  }

  @Test
  void testEmptyCanonicalNameThrowsException() {
    final Exception exception = assertThrows(Exception.class,
      () -> raw.toBuilder().setCanonicalName("").build());
    assertEquals("canonicalName should not be an empty field", exception.getMessage());
  }

  @Test
  void testProcessingMetadataDoesNotIncludeChannelGroupExpectIllegalStateException() {
    Channel.Builder testChan = UtilsTestFixtures.CHANNEL.toBuilder();
    testChan.setProcessingMetadata(Collections.EMPTY_MAP);
    final Exception exception = assertThrows(IllegalArgumentException.class,
      () -> testChan.build());

    assertEquals("processingMetadata must include an entry for CHANNEL_GROUP",
      exception.getMessage());
  }

  @Test
  void testOrientationCodeIsWhitespaceThrowsException() {
    Channel.Builder rawChannelBuilder = raw.toBuilder();
    rawChannelBuilder.setChannelOrientationType(ChannelOrientationType.UNKNOWN).setChannelOrientationCode(' ');
    final Exception exception = assertThrows(IllegalArgumentException.class,
      () ->
        rawChannelBuilder.build());

    assertEquals("channelOrientationCode cannot be whitespace", exception.getMessage());
  }

  @Test
  void testOrientationTypeCodeDoesNotMatchOrientationCodeThrowsException() {
    Channel.Builder rawChan = raw.toBuilder();
    rawChan.setChannelOrientationType(ChannelOrientationType.VERTICAL)
      .setChannelOrientationCode('N');
    final Exception exception = assertThrows(IllegalArgumentException.class,
      () -> rawChan.build());

    assertEquals(
      "channelOrientationType.code must match orientationCode when orientationType is not 'UNKNOWN'",
      exception.getMessage());
  }

  @Test
  void testOrientationTypeUnknownCodeDoesNotNeedToMatchOrientationCode() {
    assertDoesNotThrow(
      () -> raw.toBuilder().setChannelOrientationType(ChannelOrientationType.UNKNOWN)
        .setChannelOrientationCode('U').build()
    );
  }

  @Test
  void testConstruction() {
    final Location testLocation = Location.from(222.2, 333.3, 123.5, 90.2);
    final Orientation testOrientation = Orientation.from(12.1, 40.0);
    final Map<ChannelProcessingMetadataType, Object> metadata = Map
      .of(ChannelProcessingMetadataType.CHANNEL_GROUP, "CHANNEL_GROUP");

    Channel channel = Channel
      .from("TMZ123", "Test Channel", "Test Example", "parentStation",
        ChannelDataType.DIAGNOSTIC_SOH,
        ChannelBandType.BROADBAND,
        ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
        ChannelOrientationType.VERTICAL,
        'Z',
        Units.COUNTS_PER_NANOMETER,
        111.1,
        testLocation,
        testOrientation,
        List.of(),
        Map.of(),
        metadata);

    assertAll(
      () -> assertEquals("TMZ123", channel.getName()),
      () -> assertEquals("Test Channel", channel.getCanonicalName()),
      () -> assertEquals("Test Example", channel.getDescription()),
      () -> assertEquals("parentStation", channel.getStation()),
      () -> assertEquals(ChannelDataType.DIAGNOSTIC_SOH, channel.getChannelDataType()),
      () -> assertEquals(ChannelBandType.BROADBAND, channel.getChannelBandType()),
      () -> assertEquals(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
        channel.getChannelInstrumentType()),
      () -> assertEquals(ChannelOrientationType.VERTICAL, channel.getChannelOrientationType()),
      () -> assertEquals(111.1, channel.getNominalSampleRateHz()),
      () -> assertEquals(testLocation, channel.getLocation()),
      () -> assertEquals(testOrientation, channel.getOrientationAngles()),
      () -> assertEquals(List.of(), channel.getConfiguredInputs()),
      () -> assertEquals(Map.of(), channel.getProcessingDefinition()),
      () -> assertEquals(metadata, channel.getProcessingMetadata())
    );
  }

  //===============================================================================
  // Verify the hash portion of Channel name varies with particular Channel fields.
  //===============================================================================

  private static void assertHashNotEquals(Channel a, Channel b) {
    assertNotEquals(ChannelTestUtilities.extractHash(a), ChannelTestUtilities.extractHash(b));
  }

  @ParameterizedTest
  @MethodSource("provideFilterDefinitions")
  void testNameHashIncludesProcessingDefinition(FilterDefinition otherFilterDefinition) {
    final Channel other = filtered.toBuilder()
      .setProcessingDefinition(FieldMapUtilities.toFieldMap(otherFilterDefinition))
      .buildDerived();

    assertAll(
      () -> assertNotEquals(filterDefinition, otherFilterDefinition),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  private static Stream<FilterDefinition> provideFilterDefinitions() {
    return Stream.of(
      filterDefinition.toBuilder().setFilterCausality(FilterCausality.NON_CAUSAL).build(),
      filterDefinition.toBuilder().setZeroPhase(!filterDefinition.isZeroPhase()).build(),
      filterDefinition.toBuilder().setGroupDelaySecs(filterDefinition.getGroupDelaySecs() + 3.14)
        .build()
    );
  }


  @ParameterizedTest
  @MethodSource("provideConfiguredInputs")
  void testNameHashIncludesConfiguredInputs(List<Channel> otherConfiguredInputs) {
    final Channel other = filtered.toBuilder()
      .setConfiguredInputs(otherConfiguredInputs.stream()
        .map(Channel::getName).collect(Collectors.toList()))
      .buildDerived();

    for (Channel chan : otherConfiguredInputs) {
      assertAll(
        () -> assertNotEquals(filtered, chan),
        () -> assertHashNotEquals(filtered, other)
      );
    }
  }

  private static Stream<List<Channel>> provideConfiguredInputs() {
    return Stream.of(
      List.of(ChannelFactory.rawFromReferenceChannel(
        referenceChannel, STATION_NAME + "A", GROUP_NAME)),

      List.of(ChannelFactory.rawFromReferenceChannel(
        referenceChannel, STATION_NAME, GROUP_NAME + "B"))
    );
  }

  @ParameterizedTest
  @MethodSource("provideBandType")
  void testNameHashIncludesChannelBandType(ChannelBandType otherChannelBandType) {
    final Channel other = filtered.toBuilder().setChannelBandType(otherChannelBandType)
      .buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getChannelBandType(), otherChannelBandType),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  private static Stream<ChannelBandType> provideBandType() {
    return Stream.of(ChannelBandType.ADMINISTRATIVE, ChannelBandType.UNKNOWN);
  }

  @ParameterizedTest
  @MethodSource("provideInstrumentType")
  void testNameHashIncludesChannelInstrumentType(ChannelInstrumentType otherInstrumentType) {
    final Channel other = filtered.toBuilder().setChannelInstrumentType(otherInstrumentType)
      .buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getChannelInstrumentType(), otherInstrumentType),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  private static Stream<ChannelInstrumentType> provideInstrumentType() {
    return Stream.of(ChannelInstrumentType.BOLOMETER, ChannelInstrumentType.RAINFALL);
  }

  @ParameterizedTest
  @MethodSource("provideOrientationType")
  void testNameHashIncludesChannelOrientationType(ChannelOrientationType otherOrientationType) {
    final Channel other = filtered.toBuilder()
      .setChannelOrientationType(otherOrientationType)
      .setChannelOrientationCode(otherOrientationType.getCode())
      .buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getChannelOrientationType(), otherOrientationType),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  private static Stream<ChannelOrientationType> provideOrientationType() {
    return Stream.of(ChannelOrientationType.ORTHOGONAL_1, ChannelOrientationType.TRIAXIAL_C);
  }

  @ParameterizedTest
  @ValueSource(chars = {'J', '9', '~'})
  void testNameHashIncludesChannelOrientationCode(char otherOrientationCode) {
    final Channel other = filtered.toBuilder()
      .setChannelOrientationType(ChannelOrientationType.UNKNOWN)
      .setChannelOrientationCode(otherOrientationCode)
      .buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getChannelOrientationCode(), otherOrientationCode),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  @ParameterizedTest
  @MethodSource("provideDataType")
  void testNameHashIncludesChannelDataType(ChannelDataType otherDataType) {
    final Channel other = filtered.toBuilder().setChannelDataType(otherDataType).buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getChannelDataType(), otherDataType),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  private static Stream<ChannelDataType> provideDataType() {
    return Stream.of(ChannelDataType.HYDROACOUSTIC, ChannelDataType.DIAGNOSTIC_SOH);
  }

  @ParameterizedTest
  @MethodSource("provideOrientationAngles")
  void testNameHashIncludesOrientation(Orientation otherOrientation) {
    final Channel other = filtered.toBuilder().setOrientationAngles(otherOrientation)
      .buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getOrientationAngles(), otherOrientation),
      () -> assertHashNotEquals(filtered, other)
    );
  }

  private static Stream<Orientation> provideOrientationAngles() {
    return Stream.of(
      Orientation.from(1.0, 0.0),
      Orientation.from(0.0, 2.0),
      Orientation.from(-3.0, 4.0)
    );
  }

  @ParameterizedTest
  @ValueSource(doubles = {20.0, 41.0, 60.0})
  void testNameHashIncludesNominalSampleRate(double sampleRateHz) {
    final Channel other = filtered.toBuilder().setNominalSampleRateHz(sampleRateHz).buildDerived();

    assertAll(
      () -> assertNotEquals(filtered.getNominalSampleRateHz(), sampleRateHz),
      () -> assertHashNotEquals(filtered, other)
    );
  }
}
