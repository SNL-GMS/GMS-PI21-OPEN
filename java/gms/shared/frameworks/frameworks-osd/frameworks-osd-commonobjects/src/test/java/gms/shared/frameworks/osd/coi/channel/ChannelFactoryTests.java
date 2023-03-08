package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import gms.shared.frameworks.osd.coi.Units;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.BEAM_AZIMUTH_DEG;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.BEAM_COHERENT;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.BEAM_SLOWNESS_SEC_PER_DEG;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.FK_SPECTRA_DEFINITION;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.GROUP_DELAY;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.GROUP_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.HIGH;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.LOW;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.NOMINAL_SAMPLE_RATE_HZ;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.RAW_CHANNEL_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.bandType;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.beamDefinition;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.beamed;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.causality;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.filterDefinition;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.filtered;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.fked;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.instrumentType;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.location;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.orientationAngles;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.orientationType;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.passBandType;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.raw;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.referenceChannel;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.type;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelFactoryTests {

  @Test
  void testRawFromReferenceChannel() {

    final Channel raw = ChannelFactory
      .rawFromReferenceChannel(referenceChannel, STATION_NAME, GROUP_NAME);

    final String expectedChannelName =
      STATION_NAME + Channel.NAME_SEPARATOR + GROUP_NAME + Channel.NAME_SEPARATOR + RAW_CHANNEL_NAME;

    assertAll(
      () -> assertNotNull(raw),
      () -> assertEquals(expectedChannelName, raw.getName()),
      () -> assertEquals(expectedChannelName, raw.getCanonicalName()),
      () -> assertEquals(STATION_NAME, raw.getStation()),

      () -> assertEquals(ChannelDataType.SEISMIC, raw.getChannelDataType()),
      () -> assertEquals(bandType, raw.getChannelBandType()),
      () -> assertEquals(instrumentType, raw.getChannelInstrumentType()),
      () -> assertEquals(orientationType, raw.getChannelOrientationType()),
      () -> assertEquals(orientationType.getCode(),
        raw.getChannelOrientationCode()),

      () -> assertEquals(location, raw.getLocation()),
      () -> assertEquals(orientationAngles, raw.getOrientationAngles()),

      () -> assertEquals(NOMINAL_SAMPLE_RATE_HZ, raw.getNominalSampleRateHz()),
      () -> assertEquals(Units.COUNTS_PER_NANOMETER, raw.getUnits()),

      () -> assertTrue(raw.getConfiguredInputs().isEmpty()),
      () -> assertTrue(raw.getProcessingDefinition().isEmpty()),

      () -> assertEquals(1, raw.getProcessingMetadata().size()),
      () -> assertEquals(GROUP_NAME,
        raw.getProcessingMetadata().get(ChannelProcessingMetadataType.CHANNEL_GROUP)),

      () -> assertEquals(
        String.format("Raw Channel created from ReferenceChannel %s with version %s",
          referenceChannel.getEntityId(), referenceChannel.getVersionId()),
        raw.getDescription())
    );
  }

  @Test
  void testRawFromReferenceChannelValidatesInputs() {
    assertAll(
      () -> assertThrowsNullPointerException(
        () -> ChannelFactory.rawFromReferenceChannel(null, "sta", "group"),
        "referenceChannel can't be null"),

      () -> assertThrowsNullPointerException(
        () -> ChannelFactory
          .rawFromReferenceChannel(referenceChannel, null, "group"),
        "stationName can't be null"),

      () -> assertThrowsNullPointerException(
        () -> ChannelFactory
          .rawFromReferenceChannel(referenceChannel, "sta", null),
        "channelGroupName can't be null")
    );
  }

  @Test
  void testFiltered() {
    final String expectedFilterAttributes = Channel.COMPONENT_SEPARATOR + "filter"
      + Channel.ATTRIBUTE_SEPARATOR + filterDefinition.getFilterType().toString()
      .toLowerCase(Locale.ENGLISH)
      + Channel.ATTRIBUTE_SEPARATOR
      + filterDefinition.getFilterPassBandType().toString().toLowerCase(Locale.ENGLISH)
      + "_0.00hz_5.00hz"
      .toLowerCase(Locale.ENGLISH);

    final String expectedName = raw.getName()
      + expectedFilterAttributes
      + Channel.COMPONENT_SEPARATOR;

    final Map<ChannelProcessingMetadataType, Object> expectedMetadata =
      new EnumMap<>(raw.getProcessingMetadata());

    expectedMetadata.put(ChannelProcessingMetadataType.FILTER_TYPE, type);
    expectedMetadata.put(ChannelProcessingMetadataType.FILTER_PASS_BAND_TYPE, passBandType);
    expectedMetadata.put(ChannelProcessingMetadataType.FILTER_LOW_FREQUENCY_HZ, LOW);
    expectedMetadata.put(ChannelProcessingMetadataType.FILTER_HIGH_FREQUENCY_HZ, HIGH);
    expectedMetadata.put(ChannelProcessingMetadataType.FILTER_CAUSALITY, causality);
    expectedMetadata.put(ChannelProcessingMetadataType.FILTER_GROUP_DELAY, GROUP_DELAY);

    assertAll(
      () -> assertNotNull(filtered),

      () -> assertTrue(filtered.getName().startsWith(expectedName)),
      () -> assertEquals(filtered.getName(), filtered.getCanonicalName()),
      () -> assertChannelNameEndsWithUuid(filtered),
      () -> assertEquals(raw.getStation(), filtered.getStation()),

      () -> assertEquals(raw.getChannelDataType(), filtered.getChannelDataType()),
      () -> assertEquals(raw.getChannelBandType(), filtered.getChannelBandType()),
      () -> assertEquals(raw.getChannelInstrumentType(), filtered.getChannelInstrumentType()),
      () -> assertEquals(raw.getChannelOrientationType(), filtered.getChannelOrientationType()),
      () -> assertEquals(raw.getChannelOrientationCode(), filtered.getChannelOrientationCode()),

      () -> assertEquals(raw.getLocation(), filtered.getLocation()),
      () -> assertEquals(raw.getOrientationAngles(), filtered.getOrientationAngles()),

      () -> assertEquals(raw.getNominalSampleRateHz(), filtered.getNominalSampleRateHz()),
      () -> assertEquals(raw.getUnits(), filtered.getUnits()),

      () -> assertEquals(List.of(raw.getName()), filtered.getConfiguredInputs()),
      () -> assertEquals(FieldMapUtilities.toFieldMap(filterDefinition),
        filtered.getProcessingDefinition()),

      () -> assertEquals(expectedMetadata, filtered.getProcessingMetadata()),

      () -> assertEquals(
        "Filtered channel created by applying " +
          filterDefinition.getDescription() + " to " + raw.getName(),
        filtered.getDescription())
    );
  }

  @Test
  void testFilteredDerivedHasCorrectName() {
    final Channel filteredBeam = ChannelFactory.filtered(beamed, filterDefinition);
    final String name = filteredBeam.getName();

    final String expectedStaGroupChanName = STATION_NAME
      + Channel.NAME_SEPARATOR + "beam"
      + Channel.NAME_SEPARATOR + RAW_CHANNEL_NAME;

    assertAll(
      () -> assertTrue(name.startsWith(expectedStaGroupChanName)),
      () -> assertChannelAttributesContainsOnly(filteredBeam, List.of(beamed, filtered)),
      () -> assertChannelNameEndsWithUuid(filteredBeam)
    );
  }

  @Test
  void testFilteredValidatesInputs() {
    assertAll(
      () -> assertThrowsNullPointerException(
        () -> ChannelFactory.filtered(null, filterDefinition), "input can't be null"),

      () -> assertThrowsNullPointerException(() -> ChannelFactory.filtered(raw, null),
        "filterDefinition can't be null")
    );
  }

  @Test
  void testBeamed() {
    final String expectedBeamAttributes = Channel.COMPONENT_SEPARATOR + "beam"
      + Channel.ATTRIBUTE_SEPARATOR + BEAM_COHERENT
      + Channel.COMPONENT_SEPARATOR + "steer"
      + Channel.ATTRIBUTE_SEPARATOR + "az_67.300deg"
      + Channel.ATTRIBUTE_SEPARATOR + "slow_4.320s_per_deg";

    final String expectedStaGroupChanName = STATION_NAME
      + Channel.NAME_SEPARATOR + "beam"
      + Channel.NAME_SEPARATOR + RAW_CHANNEL_NAME;

    final String expectedName = expectedStaGroupChanName
      + expectedBeamAttributes
      + Channel.COMPONENT_SEPARATOR;

    final Map<ChannelProcessingMetadataType, Object> expectedMetadata = new EnumMap<>(
      raw.getProcessingMetadata());
    expectedMetadata.put(ChannelProcessingMetadataType.CHANNEL_GROUP, "beam");
    expectedMetadata
      .put(ChannelProcessingMetadataType.STEERING_AZIMUTH, BEAM_AZIMUTH_DEG);
    expectedMetadata.put(ChannelProcessingMetadataType.STEERING_SLOWNESS,
      BEAM_SLOWNESS_SEC_PER_DEG);
    expectedMetadata
      .put(ChannelProcessingMetadataType.BEAM_COHERENT, BEAM_COHERENT);

    final Channel beamed = ChannelFactory.beamed(STATION, List.of(raw), beamDefinition);

    assertAll(
      () -> assertNotNull(beamed),

      () -> assertTrue(beamed.getName().startsWith(expectedName)),
      () -> assertEquals(beamed.getName(), beamed.getCanonicalName()),
      () -> assertChannelNameEndsWithUuid(beamed),
      () -> assertEquals(raw.getStation(), beamed.getStation()),

      () -> assertEquals(raw.getChannelDataType(), beamed.getChannelDataType()),
      () -> assertEquals(raw.getChannelBandType(), beamed.getChannelBandType()),
      () -> assertEquals(raw.getChannelInstrumentType(), beamed.getChannelInstrumentType()),
      () -> assertEquals(raw.getChannelOrientationType(), beamed.getChannelOrientationType()),
      () -> assertEquals(raw.getChannelOrientationCode(), beamed.getChannelOrientationCode()),

      () -> assertEquals(STATION.getLocation(), beamed.getLocation()),
      () -> assertEquals(Orientation.from(Double.NaN, 0.0), beamed.getOrientationAngles()),

      () -> assertEquals(beamDefinition.getNominalWaveformSampleRate(),
        beamed.getNominalSampleRateHz()),
      () -> assertEquals(raw.getUnits(), beamed.getUnits()),

      () -> assertEquals(List.of(raw.getName()), beamed.getConfiguredInputs()),
      () -> assertEquals(FieldMapUtilities.toFieldMap(beamDefinition),
        beamed.getProcessingDefinition()),

      () -> assertEquals(expectedMetadata, beamed.getProcessingMetadata()),

      () -> assertEquals("Beam created for " + STATION.getName() + " with location " + STATION.getLocation(),
        beamed.getDescription())
    );
  }

  @Test
  void testBeamedNorthOrientatedChannels() {
    final Channel beamed = ChannelFactory
      .beamed(STATION,
        List.of(channelWithOrientationType(raw, ChannelOrientationType.NORTH_SOUTH)),
        beamDefinition);

    assertEquals(Orientation.from(0.0, 90.0), beamed.getOrientationAngles());
  }

  @Test
  void testBeamedEastOrientatedChannels() {
    final Channel beamed = ChannelFactory
      .beamed(STATION, List.of(channelWithOrientationType(raw, ChannelOrientationType.EAST_WEST)),
        beamDefinition);
    assertEquals(Orientation.from(90.0, 90.0), beamed.getOrientationAngles());
  }

  @Test
  void testBeamedUnsupportedChannelOrientationExpectException() {
    final ChannelOrientationType otherOrientationType = ChannelOrientationType.TRIAXIAL_C;
    List<Channel> chanList = List.of(channelWithOrientationType(raw, otherOrientationType));

    assertAll(
      () -> assertNotEquals(raw.getChannelOrientationType(), otherOrientationType),
      () -> assertThrows(IllegalStateException.class, () -> ChannelFactory
        .beamed(STATION, chanList,
          beamDefinition))
    );
  }

  /**
   * Construct a new {@link Channel} which is the same as the provided channel but with the provided
   * {@link ChannelOrientationType} and corresponding {@link ChannelOrientationType#getCode()}
   *
   * @param channel template {@link Channel} used to construct the new Channel, not null
   * @param orientationType {@link ChannelOrientationType} for the new Channel, not null
   * @return {@link Channel}, not null
   */
  private Channel channelWithOrientationType(Channel channel,
    ChannelOrientationType orientationType) {

    return channel.toBuilder().
      setChannelOrientationType(orientationType)
      .setChannelOrientationCode(orientationType.getCode())
      .build();
  }

  @Test
  void testBeamedValidatesInputs() {
    assertAll(
      () -> assertThrowsNullPointerException(
        () -> ChannelFactory.beamed(STATION, null, beamDefinition), "inputs can't be null"),

      () -> assertThrowsNullPointerException(
        () -> ChannelFactory.beamed(STATION, List.of(raw), null),
        "beamDefinition can't be null")
    );
  }

  /**
   * Verifies name contains filtered and beam attributes when filtering beamed channel.
   */
  @Test
  void testFilteredBeamHasCorrectNameAttributes() {
    final Channel beamedFiltered = ChannelFactory
      .beamed(STATION, List.of(filtered), beamDefinition);

    final Map<ChannelProcessingMetadataType, Object> expectedMetadata = new EnumMap<>(
      ChannelProcessingMetadataType.class);
    expectedMetadata.putAll(filtered.getProcessingMetadata());
    expectedMetadata.putAll(beamed.getProcessingMetadata());

    assertAll(
      () -> assertNotNull(beamedFiltered),
      () -> assertEquals(expectedMetadata.size(), beamedFiltered.getProcessingMetadata().size()),
      () -> assertEquals(expectedMetadata, beamedFiltered.getProcessingMetadata()),
      () -> assertChannelAttributesContainsOnly(beamedFiltered, List.of(beamed, filtered))
    );
  }

  private static void assertChannelAttributesContainsOnly(Channel channel, List<Channel> expected) {
    final List<String> expectedAttributes = expected.stream()
      .map(ChannelTestUtilities::extractAttributes)
      .collect(Collectors.toList());

    final String attributes = ChannelTestUtilities.extractAttributes(channel);
    assertTrue(expectedAttributes.stream().allMatch(attributes::contains));

    // Attributes length is equal to the sum of the lengths of the expected attributes minus the
    // number of attribute separators counted twice during the summation (dual counting occurs
    // since the extractAttributes operation returns Strings that start and end with the separator).
    final int expectedLength = expectedAttributes.stream().mapToInt(String::length).sum() -
      (expected.size() - 1);
    assertEquals(expectedLength, attributes.length());
  }

  /**
   * Verifies the provided {@link Channel}'s name ends with a UUID
   *
   * @param channel {@link Channel}, not null
   */
  private static void assertChannelNameEndsWithUuid(Channel channel) {
    assertDoesNotThrow(() -> UUID.fromString(ChannelTestUtilities.extractHash(channel)));
  }

  /**
   * Verifies executing the {@link Executable} produces a NullPointerException with the provided
   * message.
   *
   * @param executable {@link Executable} to invoke, not null
   * @param message expected exception message, not null
   */
  private static void assertThrowsNullPointerException(Executable executable, String message) {
    assertEquals(message, assertThrows(NullPointerException.class, executable).getMessage());
  }

  @Test
  void testFKed() {
    final String expectedName = STATION_NAME
      + Channel.NAME_SEPARATOR + "fk"
      + Channel.NAME_SEPARATOR + RAW_CHANNEL_NAME;

    final Map<ChannelProcessingMetadataType, Object> expectedMetadata =
      new EnumMap<>(raw.getProcessingMetadata());

    expectedMetadata.put(ChannelProcessingMetadataType.CHANNEL_GROUP, "fk");

    assertAll(
      () -> assertNotNull(fked, "channel"),

      () -> assertTrue(fked.getName().startsWith(expectedName), "name"),
      () -> assertEquals(fked.getName(), fked.getCanonicalName(), "canonicalName"),
      () -> assertChannelNameEndsWithUuid(fked),
      () -> assertEquals(raw.getStation(), fked.getStation(), "station"),

      () -> assertEquals(raw.getChannelDataType(), fked.getChannelDataType(), "channelDataType"),
      () -> assertEquals(raw.getChannelBandType(), fked.getChannelBandType(), "channelBandType"),
      () -> assertEquals(raw.getChannelInstrumentType(), fked.getChannelInstrumentType(),
        "channelInstrumentType"),
      () -> assertEquals(raw.getChannelOrientationType(), fked.getChannelOrientationType(),
        "channelOrientationType"),
      () -> assertEquals(raw.getChannelOrientationCode(), fked.getChannelOrientationCode(),
        "channelOrientationCode"),

      () -> assertEquals(STATION.getLocation(), fked.getLocation(), "location"),
      () -> assertEquals(raw.getOrientationAngles(), fked.getOrientationAngles(),
        "orientationAngles"),

      () -> assertEquals(FK_SPECTRA_DEFINITION.getWaveformSampleRateHz(),
        fked.getNominalSampleRateHz(),
        "sampleRate"),
      () -> assertEquals(raw.getUnits(), fked.getUnits(), "units"),

      () -> assertEquals(List.of(raw.getName()), fked.getConfiguredInputs(), "configuredInputs"),
      () -> assertEquals(FieldMapUtilities.toFieldMap(FK_SPECTRA_DEFINITION),
        fked.getProcessingDefinition(), "definition"),

      () -> assertEquals(expectedMetadata, fked.getProcessingMetadata(), "processingMetadata")
    );
  }
}