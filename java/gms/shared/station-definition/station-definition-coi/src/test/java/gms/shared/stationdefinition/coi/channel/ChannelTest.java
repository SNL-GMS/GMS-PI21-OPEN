package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.coi.channel.Channel.Builder;
import gms.shared.stationdefinition.coi.channel.Channel.Data;
import gms.shared.stationdefinition.coi.id.VersionId;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.StationDefinitionObject;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.createTestChannelData;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ChannelTest {
  private static final Object NULL_OBJECT = null;

  private static final Logger logger = LoggerFactory.getLogger(ChannelTest.class);

  private final static ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(CHANNEL, Channel.class);
  }

  @Test
  void testEmptyNameThrowsException() {
    final Builder builder = Channel.builder()
      .setName("")
      .setData(UtilsTestFixtures.createChannelDataBuilder(CHANNEL).build());
    final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);
    assertEquals("Channel must be provided a name", exception.getMessage());
  }

  @Test
  void testEmptyCanonicalNameThrowsException() {
    final var channel = CHANNEL;
    final Data.Builder builder = UtilsTestFixtures.createChannelDataBuilder(channel)
      .setCanonicalName("");
    final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);
    assertEquals("Channel must be provided a canonical name", exception.getMessage());
  }

  @Test
  void testProcessingMetadataDoesNotIncludeChannelGroupExpectIllegalStateException() {
    final var channel = CHANNEL;
    final Data.Builder builder = UtilsTestFixtures.createChannelDataBuilder(channel)
      .setProcessingMetadata(Map.of());
    final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);
    assertEquals("Channel's processingMetadata must include an entry for CHANNEL_GROUP",
      exception.getMessage());
  }

  @Test
  void testOrientationCodeIsWhitespaceThrowsException() {
    final Data.Builder builder = UtilsTestFixtures.createChannelDataBuilder(CHANNEL)
      .setChannelOrientationType(ChannelOrientationType.UNKNOWN)
      .setChannelOrientationCode(' ');
    final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

    assertEquals("Channel's channelOrientationCode cannot be whitespace", exception.getMessage());
  }

  @Test
  void testOrientationTypeCodeDoesNotMatchOrientationCodeThrowsException() {
    final var channel = CHANNEL;
    final Data.Builder builder = UtilsTestFixtures.createChannelDataBuilder(channel)
      .setChannelOrientationType(ChannelOrientationType.VERTICAL)
      .setChannelOrientationCode('N');
    final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

    assertEquals(
      "channelOrientationType.code must match orientationCode when orientationType is not 'UNKNOWN'",
      exception.getMessage());
  }

  @Test
  void testOrientationTypeUnknownCodeDoesNotNeedToMatchOrientationCode() {
    final var channel = CHANNEL;
    assertDoesNotThrow(
      () -> channel.toBuilder()
        .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
          .setChannelOrientationType(ChannelOrientationType.UNKNOWN)
          .setChannelOrientationCode('U')
          .build())
        .build());
  }

  @Test
  void testConstruction() {
    final Location testLocation = Location.from(222.2, 333.3, 123.5, 90.2);
    final Orientation testOrientation = Orientation.from(12.1, 40.0);
    final Map<ChannelProcessingMetadataType, Object> metadata = Map
      .of(ChannelProcessingMetadataType.CHANNEL_GROUP, "CHANNEL_GROUP");

    final String channelName = "TMZ123";
    final Response response = UtilsTestFixtures.getResponse(channelName);
    Channel channel = Channel.builder()
      .setName(channelName)
      .setEffectiveAt(Instant.EPOCH)
      .setData(Channel.Data.builder()
        .setCanonicalName("Test Channel")
        .setEffectiveUntil(Instant.EPOCH.plusSeconds(10))
        .setDescription("Test Example")
        .setStation(Station.createEntityReference("parentStation"))
        .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.VERTICAL)
        .setChannelOrientationCode('Z')
        .setUnits(Units.COUNTS_PER_NANOMETER)
        .setNominalSampleRateHz(111.1)
        .setLocation(testLocation)
        .setOrientationAngles(testOrientation)
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(metadata)
        .setResponse(response)
        .build())
      .build();

    assertAll(
      () -> assertEquals(channelName, channel.getName()),
      () -> assertEquals("Test Channel", channel.getCanonicalName()),
      () -> assertEquals(Instant.EPOCH, channel.getEffectiveAt().orElseThrow()),
      () -> assertEquals(Instant.EPOCH.plusSeconds(10), channel.getEffectiveUntil().orElseThrow()),
      () -> assertEquals("Test Example", channel.getDescription()),
      () -> assertEquals("parentStation", channel.getStation().getName()),
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
      () -> assertEquals(metadata, channel.getProcessingMetadata()),
      () -> assertEquals(Optional.of(response), channel.getResponse())
    );
  }

  @Test
  void testConstruction_nullResponse() {
    final Location testLocation = Location.from(222.2, 333.3, 123.5, 90.2);
    final Orientation testOrientation = Orientation.from(12.1, 40.0);
    final Map<ChannelProcessingMetadataType, Object> metadata = Map
      .of(ChannelProcessingMetadataType.CHANNEL_GROUP, "CHANNEL_GROUP");

    final String channelName = "TMZ123";
    Channel channel = Channel.builder()
      .setName(channelName)
      .setEffectiveAt(Instant.now())
      .setData(Channel.Data.builder()
        .setCanonicalName("Test Channel")
        .setDescription("Test Example")
        .setStation(Station.createEntityReference("parentStation"))
        .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.VERTICAL)
        .setChannelOrientationCode('Z')
        .setUnits(Units.COUNTS_PER_NANOMETER)
        .setNominalSampleRateHz(111.1)
        .setLocation(testLocation)
        .setOrientationAngles(testOrientation)
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(metadata)
        .setResponse(Optional.empty())
        .build())
      .build();

    assertAll(
      () -> assertEquals(channelName, channel.getName()),
      () -> assertEquals("Test Channel", channel.getCanonicalName()),
      () -> assertEquals("Test Example", channel.getDescription()),
      () -> assertEquals("parentStation", channel.getStation().getName()),
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
      () -> assertEquals(metadata, channel.getProcessingMetadata()),
      () -> assertEquals(Optional.empty(), channel.getResponse())
    );
  }

  @Test
  void testConstruction_responseNotSet() {
    final Location testLocation = Location.from(222.2, 333.3, 123.5, 90.2);
    final Orientation testOrientation = Orientation.from(12.1, 40.0);
    final Map<ChannelProcessingMetadataType, Object> metadata = Map
      .of(ChannelProcessingMetadataType.CHANNEL_GROUP, "CHANNEL_GROUP");

    final String channelName = "TMZ123";
    Channel channel = Channel.builder()
      .setName(channelName)
      .setEffectiveAt(Instant.now())
      .setData(Channel.Data.builder()
        .setCanonicalName("Test Channel")
        .setDescription("Test Example")
        .setStation(Station.createEntityReference("parentStation"))
        .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.VERTICAL)
        .setChannelOrientationCode('Z')
        .setUnits(Units.COUNTS_PER_NANOMETER)
        .setNominalSampleRateHz(111.1)
        .setLocation(testLocation)
        .setOrientationAngles(testOrientation)
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(metadata)
        .build())
      .build();

    assertAll(
      () -> assertEquals(channelName, channel.getName()),
      () -> assertEquals("Test Channel", channel.getCanonicalName()),
      () -> assertEquals("Test Example", channel.getDescription()),
      () -> assertEquals("parentStation", channel.getStation().getName()),
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
      () -> assertEquals(metadata, channel.getProcessingMetadata()),
      () -> assertEquals(Optional.empty(), channel.getResponse())
    );
  }

  //===============================================================================
  // Verify the hash portion of Channel name varies with particular Channel fields.
  //===============================================================================

  @ParameterizedTest
  @MethodSource("provideBandType")
  void testNameHashIncludesChannelBandType(ChannelBandType otherChannelBandType) {
    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setChannelBandType(otherChannelBandType)
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getChannelBandType(), otherChannelBandType),
      () -> assertEquals(otherChannelBandType, other.getChannelBandType())
    );
  }

  private static Stream<ChannelBandType> provideBandType() {
    return Stream.of(ChannelBandType.ADMINISTRATIVE, ChannelBandType.UNKNOWN);
  }

  @ParameterizedTest
  @MethodSource("provideInstrumentType")
  void testNameHashIncludesChannelInstrumentType(ChannelInstrumentType otherInstrumentType) {
    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setChannelInstrumentType(otherInstrumentType)
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getChannelInstrumentType(), otherInstrumentType),
      () -> assertEquals(otherInstrumentType, other.getChannelInstrumentType())
    );
  }

  private static Stream<ChannelInstrumentType> provideInstrumentType() {
    return Stream.of(ChannelInstrumentType.BOLOMETER, ChannelInstrumentType.RAINFALL);
  }

  @ParameterizedTest
  @MethodSource("provideOrientationType")
  void testNameHashIncludesChannelOrientationType(ChannelOrientationType otherOrientationType) {
    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setChannelOrientationType(otherOrientationType)
        .setChannelOrientationCode(otherOrientationType.getCode())
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getChannelOrientationType(), otherOrientationType),
      () -> assertEquals(otherOrientationType, other.getChannelOrientationType())
    );
  }

  private static Stream<ChannelOrientationType> provideOrientationType() {
    return Stream.of(ChannelOrientationType.ORTHOGONAL_1, ChannelOrientationType.TRIAXIAL_C);
  }

  @ParameterizedTest
  @ValueSource(chars = {'J', '9', '~'})
  void testNameHashIncludesChannelOrientationCode(char otherOrientationCode) {
    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setChannelOrientationType(ChannelOrientationType.UNKNOWN)
        .setChannelOrientationCode(otherOrientationCode)
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getChannelOrientationCode(), otherOrientationCode),
      () -> assertEquals(otherOrientationCode, other.getChannelOrientationCode())
    );
  }

  @ParameterizedTest
  @MethodSource("provideDataType")
  void testNameHashIncludesChannelDataType(ChannelDataType otherDataType) {

    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setChannelDataType(otherDataType)
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getChannelDataType(), otherDataType),
      () -> assertEquals(otherDataType, other.getChannelDataType())
    );
  }

  private static Stream<ChannelDataType> provideDataType() {
    return Stream.of(ChannelDataType.HYDROACOUSTIC, ChannelDataType.INFRASOUND);
  }

  @ParameterizedTest
  @MethodSource("provideOrientationAngles")
  void testNameHashIncludesOrientation(Orientation otherOrientation) {
    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setOrientationAngles(otherOrientation)
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getOrientationAngles(), otherOrientation),
      () -> assertEquals(otherOrientation, other.getOrientationAngles())
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
    final var channel = CHANNEL;
    final Channel other = channel.toBuilder()
      .setData(UtilsTestFixtures.createChannelDataBuilder(channel)
        .setNominalSampleRateHz(sampleRateHz)
        .build())
      .build();

    assertAll(
      () -> assertNotEquals(channel.getNominalSampleRateHz(), sampleRateHz),
      () -> assertEquals(sampleRateHz, other.getNominalSampleRateHz())
    );
  }

  @Test
  void testChannel_createEntityReference_serializeToAndFrom()
    throws JsonProcessingException {
    final Channel channel = getNameFacetChannel("test");

    final String json = mapper.writeValueAsString(channel);
    logger.info("json serialized channel: {}", json);

    final Channel deserialized = mapper.readValue(json, Channel.class);
    assertEquals(channel, deserialized);
    assertFalse(channel.isPresent());
  }

  @Test
  void testChannel_createEntityReference_present() {
    final Channel channel = getNameFacetChannel("test");

    assertFalse(channel.isPresent());
  }

  @Test
  void testChannel_createEntityReference_emptyName() {
    final var exception = assertThrows(IllegalArgumentException.class,
      () -> getNameFacetChannel(""));
    logger.info("EXPECTED ERROR: ", exception);
    assertEquals("Channel must be provided a name", exception.getMessage());
  }

  @Test
  void testChannel_from_serializeToAndFrom_entityReferenceStation() throws JsonProcessingException {
    final Channel channel = getFullChannel(Station.createEntityReference("stationOne"));

    final String json = mapper.writeValueAsString(channel);
    logger.info("json serialized channel: {}", json);

    final Channel deserialized = mapper.readValue(json, Channel.class);
    assertEquals(channel, deserialized);
  }

  @Test
  void testChannel_serializeToAndFrom_versionReferenceStation() throws JsonProcessingException {
    final Channel channel = getFullChannel(Station.createVersionReference("stationOne", Instant.now()));

    final String json = mapper.writeValueAsString(channel);
    logger.info("json serialized channel: {}", json);

    final Channel deserialized = mapper.readValue(json, Channel.class);
    assertEquals(channel, deserialized);

  }

  @Test
  void testChannel_CreateEntityReference_present() {
    final Channel channel = getFullChannel(Station.createEntityReference("stationOne"));

    assertTrue(channel.isPresent());
  }
  @Test
  void testChannel_CreateEntityReference_fromChannel() {
    final Channel channel = Channel.createEntityReference(CHANNEL);

    assertEquals(CHANNEL.getName(), channel.getName());
  }
  @ParameterizedTest
  @MethodSource("getVersionReferenceArguments")
  void testCreateVersionReferenceValidation(String name, Instant effectiveAt) {
    assertThrows(NullPointerException.class,
      () -> Channel.createVersionReference(name, effectiveAt));
  }

  static Stream<Arguments> getVersionReferenceArguments() {
    return Stream.of(arguments(NULL_OBJECT, Instant.now()),
      arguments("test", NULL_OBJECT));
  }

  @Test
  void testCreateVersionReference() {
    Channel channel = Channel.createVersionReference("test", Instant.EPOCH);

    assertNotNull(channel);
    assertEquals("test", channel.getName());
    assertTrue(channel.getEffectiveAt().isPresent());
    assertEquals(Instant.EPOCH, channel.getEffectiveAt().orElseThrow());
  }
  @Test
  void testCreateVersionReferenceFromChannel() {
    Channel channel = Channel.createVersionReference(CHANNEL);

    assertNotNull(channel);
    assertEquals(CHANNEL.getName(), channel.getName());
    assertEquals(CHANNEL.getEffectiveAt().orElseThrow(), channel.getEffectiveAt().orElseThrow());
  }
  @Test
  void testToStationEntityReference() {
    final Location testLocation = Location.from(222.2, 333.3, 123.5, 90.2);
    final Orientation testOrientation = Orientation.from(12.1, 40.0);
    final Map<ChannelProcessingMetadataType, Object> metadata = Map
      .of(ChannelProcessingMetadataType.CHANNEL_GROUP, "CHANNEL_GROUP");

    final String channelName = "TMZ123";
    final Response response = UtilsTestFixtures.getResponse(channelName);
    final String staitonName = "parentStation";
    Channel channel = Channel.builder()
      .setName(channelName)
      .setEffectiveAt(Instant.now())
      .setData(Channel.Data.builder()
        .setCanonicalName("Test Channel")
        .setDescription("Test Example")
        .setStation(Station.createEntityReference(staitonName))
        .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.VERTICAL)
        .setChannelOrientationCode('Z')
        .setUnits(Units.COUNTS_PER_NANOMETER)
        .setNominalSampleRateHz(111.1)
        .setLocation(testLocation)
        .setOrientationAngles(testOrientation)
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(metadata)
        .setResponse(response)
        .build())
      .build();

    final Station result = channel.getStation();//.toStationEntityReference();

    assertEquals(Station.createEntityReference(staitonName), result);
  }

  @Test
  void testToStationVersionReference() {
    final Location testLocation = Location.from(222.2, 333.3, 123.5, 90.2);
    final Orientation testOrientation = Orientation.from(12.1, 40.0);
    final Map<ChannelProcessingMetadataType, Object> metadata = Map
      .of(ChannelProcessingMetadataType.CHANNEL_GROUP, "CHANNEL_GROUP");

    final String channelName = "TMZ123";
    final Response response = UtilsTestFixtures.getResponse(channelName);
    final String staitonName = "parentStation";
    final Instant now = Instant.now();
    Channel channel = Channel.builder()
      .setName(channelName)
      .setEffectiveAt(now)
      .setData(Channel.Data.builder()
        .setCanonicalName("Test Channel")
        .setDescription("Test Example")
        .setStation(Station.createVersionReference(staitonName, now))
        .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.VERTICAL)
        .setChannelOrientationCode('Z')
        .setUnits(Units.COUNTS_PER_NANOMETER)
        .setNominalSampleRateHz(111.1)
        .setLocation(testLocation)
        .setOrientationAngles(testOrientation)
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(metadata)
        .setResponse(response)
        .build())
      .build();

    final Station result = channel.getStation();//.toStationVersionReference();

    assertEquals(Station.createVersionReference(staitonName, now), result);
  }

  private Channel getNameFacetChannel(String name) {
    return Channel.createEntityReference(name);
  }


  private Channel getFullChannel(Station station) {
    final String channelName = "testChannelOne";
    return Channel.builder()
      .setName(channelName)
      .setEffectiveAt(Instant.now().minusSeconds(1000))
      .setData(Channel.Data.builder()
        .setCanonicalName("Test Channel One")
        .setDescription("This is a description of the channel")
        .setStation(station)
        .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
        .setChannelBandType(ChannelBandType.BROADBAND)
        .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
        .setChannelOrientationType(ChannelOrientationType.EAST_WEST)
        .setChannelOrientationCode('E')
        .setUnits(Units.HERTZ)
        .setNominalSampleRateHz(50.0)
        .setLocation(Location.from(100.0, 10.0, 50.0, 100))
        .setOrientationAngles(Orientation.from(10.0, 35.0))
        .setConfiguredInputs(List.of())
        .setProcessingDefinition(Map.of())
        .setProcessingMetadata(
          Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "channelGroupOne"))
        .setResponse(UtilsTestFixtures.getResponse(channelName))
        .build())
      .build();
  }

  @Test
  void testToVersionIdFromEntityReference() {
    final Channel channel = Channel.createEntityReference("test");
    assertThrows(IllegalStateException.class, channel::toVersionId);
  }

  @Test
  void testToVersionId() {
    VersionId id = CHANNEL.toVersionId();
    assertEquals(CHANNEL.getName(), id.getEntityId());
    CHANNEL.getEffectiveAt()
      .ifPresentOrElse(effectiveAt -> assertEquals(effectiveAt, id.getEffectiveAt()),
        Assertions::fail);
  }

  @ParameterizedTest
  @MethodSource("getBuildValidationArguments")
  void testBuildValidation(Class<? extends Exception> expectedException,
    String channelName,
    Instant effectiveAt,
    Channel.Data data) {
    Channel.Builder builder = Channel.builder()
      .setName(channelName)
      .setEffectiveAt(effectiveAt)
      .setData(data);
    assertThrows(expectedException, builder::build);
  }

  static Stream<Arguments> getBuildValidationArguments() {
    return Stream.of(
      arguments(IllegalArgumentException.class, "", Instant.EPOCH,
        createTestChannelData("test", RESPONSE)),
      arguments(IllegalStateException.class, "test", NULL_OBJECT,
        createTestChannelData("test", RESPONSE)));
  }

  @Test
  void testBuild() {
    Channel channel = assertDoesNotThrow(() -> Channel.builder()
      .setName("test")
      .setEffectiveAt(Instant.EPOCH)
      .setData(createTestChannelData("test", RESPONSE))
      .build());
    assertNotNull(channel);
  }

  @Test
  void testSetEffectiveAt(){

    StationDefinitionObject chan = getFullChannel(Station.createEntityReference("stationOne"));
    chan = chan.setEffectiveAt(DefaultCoiTestFixtures.START);
    assertEquals(DefaultCoiTestFixtures.START, chan.getEffectiveAt().get());
  }

  @Test
  void testSetEffectiveUntil(){

    StationDefinitionObject chan = getFullChannel(Station.createEntityReference("stationOne"));
    chan = chan.setEffectiveUntil(DefaultCoiTestFixtures.END);
    assertEquals(DefaultCoiTestFixtures.END, chan.getEffectiveUntil().get());
  }

  @Test
  void testSetEffectiveAtUpdatedByResponse(){

    StationDefinitionObject chan = getFullChannel(Station.createEntityReference("stationOne"));
    chan = chan.setEffectiveAtUpdatedByResponse(false);
    assertEquals(false, chan.getEffectiveAtUpdatedByResponse().get());
  }

  @Test
  void testSetEffectiveUntilUpdatedByResponse(){

    StationDefinitionObject chan = getFullChannel(Station.createEntityReference("stationOne"));
    chan = chan.setEffectiveUntilUpdatedByResponse(false);
    assertEquals(false, chan.getEffectiveUntilUpdatedByResponse().get());
  }

}

