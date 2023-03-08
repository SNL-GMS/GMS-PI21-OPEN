package gms.shared.stationdefinition.coi.channel;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.coi.channel.ChannelGroup.ChannelGroupType;
import gms.shared.stationdefinition.coi.id.VersionId;
import gms.shared.stationdefinition.coi.utils.StationDefinitionObject;
import gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class ChannelGroupTest {
  private static final Object NULL_OBJECT = null;

  private static final Logger logger = LoggerFactory.getLogger(ChannelGroupTest.class);

  private final static ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(CHANNEL_GROUP, ChannelGroup.class);
  }

  @Test
  void testEmptyNameThrowsException() {
    assertThrows(Exception.class, () -> ChannelGroup.builder()
      .setName("")
      .setData(createTestChannelGroupData())
      .build());
  }

  @Test
  void testEmptyListOfChannelsThrowsException() {
    assertThrows(Exception.class, () -> ChannelGroup.builder()
      .setName("Test Channel Group")
      .setEffectiveAt(Instant.now())
      .setData(ChannelGroup.Data.builder()
        .setDescription("Sample Processing Group")
        .setLocation(Location.from(100.0, 50.0, 50.0, 100.0))
        .setType(ChannelGroupType.PROCESSING_GROUP)
        .setChannels(List.of())
        .build()
      ).build());
  }

  @Test
  void testChannelGroup_createEntityReference_serializeToAndFrom()
    throws JsonProcessingException {
    final ChannelGroup channelGroup = getNameFacetChannelGroup("Test channel group");

    final String json = mapper.writeValueAsString(channelGroup);
    logger.info("json serialized channelGroup: {}", json);

    final ChannelGroup deserialized = mapper.readValue(json, ChannelGroup.class);
    assertEquals(channelGroup, deserialized);
    assertFalse(channelGroup.isPresent());
  }

  @Test
  void testChannelGroup_createEntityReference_present() {
    final ChannelGroup channelGroup = getNameFacetChannelGroup("Test channel group");

    assertFalse(channelGroup.isPresent());
  }

  @Test
  void testChannelGroup_createEntityReference_emptyName() {
    final var exception = assertThrows(IllegalArgumentException.class,
      () -> getNameFacetChannelGroup(""));
    logger.info("EXPECTED ERROR: ", exception);
    assertEquals("Channel group must be provided a name", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("getVersionReferenceArguments")
  void testCreateVersionReferenceValidation(String name, Instant effectiveAt) {
    assertThrows(NullPointerException.class,
      () -> ChannelGroup.createVersionReference(name, effectiveAt));
  }

  static Stream<Arguments> getVersionReferenceArguments() {
    return Stream.of(arguments(NULL_OBJECT, Instant.now()),
      arguments("test", NULL_OBJECT));
  }

  @Test
  void testCreateVersionReference() {
    ChannelGroup channelGroup = assertDoesNotThrow(() -> ChannelGroup.createVersionReference("test", Instant.now()));
    assertNotNull(channelGroup);
  }

  @Test
  void testChannelGroup_from_serializeToAndFrom() throws JsonProcessingException {
    final ChannelGroup channelGroup = getFullChannelGroup();
    final String json = mapper.writeValueAsString(channelGroup);
    logger.info("json serialized channel group: {}", json);
    final ChannelGroup deserialized = mapper.readValue(json, ChannelGroup.class);
    assertEquals(channelGroup, deserialized);
  }

  @Test
  void testChannelGroup_from_present() {
    final ChannelGroup channelGroup = getFullChannelGroup();
    assertTrue(channelGroup.isPresent());
    assertTrue(channelGroup.getLocation().isPresent());
  }

  @Test
  void testChannelGroup_from_serializeToAndFrom_withFacetedChannels()
    throws JsonProcessingException {
    final ChannelGroup channelGroup = getFullChannelGroupWithFacetedChannels();

    final String json = mapper.writeValueAsString(channelGroup);
    logger.info("json serialized channel group: {}", json);

    final ChannelGroup deserialized = mapper.readValue(json, ChannelGroup.class);
    assertEquals(channelGroup, deserialized);
  }

  @Test
  void testChannelGroup_from_present_withFacetedChannels() {
    final ChannelGroup channelGroup = getFullChannelGroupWithFacetedChannels();

    assertTrue(channelGroup.isPresent());
    assertTrue(channelGroup.getChannels().stream().noneMatch(Channel::isPresent));
  }

  @Test
  void testChannelGroup_from_serializeToAndFrom_withNullLocation()
    throws JsonProcessingException {
    ChannelGroup channelGroup = getFullChannelGroupNullLocation();
    final String json = mapper.writeValueAsString(channelGroup);
    logger.info("json serialized channel group: {}", json);
    final ChannelGroup deserialized = mapper.readValue(json, ChannelGroup.class);
    assertEquals(channelGroup, deserialized);

  }

  @Test
  void testChannelGroup_from_present_withNullLocation() {
    final ChannelGroup channelGroup = getFullChannelGroupNullLocation();
    assertTrue(channelGroup.isPresent());
    assertFalse(channelGroup.getLocation().isPresent());
  }

  private ChannelGroup getNameFacetChannelGroup(String name) {
    return ChannelGroup.createEntityReference(name);
  }

  private ChannelGroup getFullChannelGroup() {
    return ChannelGroup.builder()
      .setName("Channel Group Full")
      .setEffectiveAt(Instant.now())
      .setData(ChannelGroup.Data.builder()
        .setDescription("Sample channel group containing all test suite channels")
        .setLocation(Location.from(100.0, 50.0, 50.0, 100.0))
        .setEffectiveUntil((CssDaoAndCoiParameters.END_TIME))
        .setType(ChannelGroupType.PHYSICAL_SITE)
        .setChannels(List.of(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.CHANNEL_TWO))
        .build()).build();

  }

  private ChannelGroup getFullChannelGroupNullLocation() {
    return ChannelGroup.builder()
      .setName("Channel Group Full")
      .setEffectiveAt(Instant.now())
      .setData(ChannelGroup.Data.builder()
        .setDescription("Sample channel group containing all test suite channels")
        .setLocation(null)
        .setEffectiveUntil(CssDaoAndCoiParameters.END_TIME)
        .setType(ChannelGroupType.PHYSICAL_SITE)
        .setChannels(List.of(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.CHANNEL_TWO))
        .build()).build();
  }

  private ChannelGroup getFullChannelGroupWithFacetedChannels() {
    return ChannelGroup.builder()
      .setName("Channel Group Full")
      .setEffectiveAt(Instant.now())
      .setData(ChannelGroup.Data.builder()
        .setDescription("Sample channel group containing all test suite channels")
        .setLocation(Location.from(100.0, 50.0, 50.0, 100.0))
        .setEffectiveUntil(CssDaoAndCoiParameters.END_TIME)
        .setType(ChannelGroupType.PHYSICAL_SITE)
        .setChannels(List.of(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.CHANNEL_TWO).stream()
          .map(c -> Channel.createEntityReference(c.getName()))
          .collect(Collectors.toList()))
        .build()
      ).build();
  }

  private static ChannelGroup.Data createTestChannelGroupData() {
    return ChannelGroup.Data.builder()
      .setDescription("Channel Group Description")
      .setLocation(Location.from(35.0,
        -125.0,
        100.0,
        5500.0))
      .setEffectiveUntil(CssDaoAndCoiParameters.END_TIME)
      .setChannels(List.of(UtilsTestFixtures.CHANNEL))
      .setType(ChannelGroupType.PROCESSING_GROUP)
      .build();
  }

  @Test
  void testToVersionIdValidation() {
    assertThrows(IllegalStateException.class, () -> getNameFacetChannelGroup("test").toVersionId());
  }

  @Test
  void testToVersionid() {
    VersionId versionId = CHANNEL_GROUP.toVersionId();
    assertEquals(CHANNEL_GROUP.getName(), versionId.getEntityId());
    CHANNEL_GROUP.getEffectiveAt().ifPresentOrElse(effectiveAt -> assertEquals(effectiveAt, versionId.getEffectiveAt()),
      () -> fail());
  }

  @ParameterizedTest
  @MethodSource("getBuildValidationArguments")
  void testBuildValidation(Class<? extends Exception> expectedException,
    String name,
    Instant effectiveAt,
    ChannelGroup.Data data) {
    ChannelGroup.Builder builder = ChannelGroup.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .setData(data);
    assertThrows(expectedException, () -> builder.build());
  }

  static Stream<Arguments> getBuildValidationArguments() {
    return Stream.of(
      arguments(IllegalArgumentException.class, "", Instant.EPOCH, UtilsTestFixtures.createTestChannelGroupData()),
      arguments(IllegalStateException.class, "Test", null, UtilsTestFixtures.createTestChannelGroupData()));
  }

  @Test
  void testBuild() {
    ChannelGroup channelGroup = assertDoesNotThrow(() -> ChannelGroup.builder()
      .setName("Test")
      .setEffectiveAt(Instant.EPOCH)
      .setData(createTestChannelGroupData())
      .build());

    assertNotNull(channelGroup);
  }

  @Test
  void testSetEffectiveAt(){

    StationDefinitionObject chanGroup = getFullChannelGroup();
    chanGroup = chanGroup.setEffectiveAt(DefaultCoiTestFixtures.START);
    assertEquals(DefaultCoiTestFixtures.START, chanGroup.getEffectiveAt().get());
  }

  @Test
  void testSetEffectiveUntil(){

    StationDefinitionObject chanGroup = getFullChannelGroup();
    chanGroup = chanGroup.setEffectiveUntil(DefaultCoiTestFixtures.END);
    assertEquals(DefaultCoiTestFixtures.END, chanGroup.getEffectiveUntil().get());
  }

  @Test
  void testSetEffectiveAtUpdatedByResponse(){

    StationDefinitionObject chanGroup = getFullChannelGroup();
    chanGroup = chanGroup.setEffectiveAtUpdatedByResponse(false);
    assertEquals(false, chanGroup.getEffectiveAtUpdatedByResponse().get());
  }

  @Test
  void testSetEffectiveUntilUpdatedByResponse(){

    StationDefinitionObject chanGroup = getFullChannelGroup();
    chanGroup = chanGroup.setEffectiveUntilUpdatedByResponse(false);
    assertEquals(false, chanGroup.getEffectiveUntilUpdatedByResponse().get());
  }
  @Test
  void testToEntityReferenceAndCreateEntityReference(){

    var chanGroup = getFullChannelGroup();
    var entityReference = chanGroup.toEntityReference();

    assertThrows(IllegalStateException.class, () -> entityReference.getDescription());
    assertThrows(IllegalStateException.class, () -> entityReference.getEffectiveUntil());
    assertThrows(IllegalStateException.class, () -> entityReference.getEffectiveAtUpdatedByResponse());
    assertThrows(IllegalStateException.class, () -> entityReference.getEffectiveUntilUpdatedByResponse());
    assertEquals(chanGroup.getName(), entityReference.getName());
    var entityReference2 = ChannelGroup.createEntityReference(chanGroup);
    assertEquals(entityReference, entityReference2);
  }

  @Test
  void testCreateVersionReferenceFromChannelGroup(){

    StationDefinitionObject chanGroup = getFullChannelGroup();
    chanGroup=chanGroup.setEffectiveAt(DefaultCoiTestFixtures.START);
    var versionReference = ChannelGroup.createVersionReference((ChannelGroup) chanGroup);
    assertThrows(IllegalStateException.class, () -> versionReference.getDescription());
    assertThrows(IllegalStateException.class, () -> versionReference.getEffectiveUntil());
    assertEquals(chanGroup.getName(), versionReference.getName());
    assertEquals(DefaultCoiTestFixtures.START, versionReference.getEffectiveAt().get());
  }

}

