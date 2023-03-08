package gms.shared.frameworks.osd.coi.channel.soh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link AcquiredChannelEnvironmentIssue} creation and usage semantics Created by trsault on
 * 8/25/17.
 */

class AcquiredChannelEnvironmentIssueTests {

  private final AcquiredChannelEnvironmentIssueType calib = AcquiredChannelEnvironmentIssueType.CALIBRATION_UNDERWAY;
  private final Instant epoch = Instant.EPOCH;
  private final Instant later = epoch.plusSeconds(30);
  private final String channelName = UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME;

  @Test
  void testSerializationAnalog() throws Exception {
    TestUtilities.testSerialization(UtilsTestFixtures.channelSohAnalog,
      AcquiredChannelEnvironmentIssueAnalog.class);
  }

  @Test
  void testSerializationBoolean() throws Exception {
    TestUtilities.testSerialization(UtilsTestFixtures.channelSohBoolean,
      AcquiredChannelEnvironmentIssueBoolean.class);
  }

  @Test
  void testAcquiredChannelSohAnalogFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      AcquiredChannelEnvironmentIssueAnalog.class, "from",
      channelName,
      calib, epoch, later, 0.0);
  }

  @Test
  void testAcquiredChannelSohBooleanFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      AcquiredChannelEnvironmentIssueBoolean.class, "from",
      channelName,
      calib, epoch, later,
      false);
  }

  @Test
  void testEncloses() {
    AcquiredChannelEnvironmentIssueBoolean enclosingAcei = AcquiredChannelEnvironmentIssueBoolean
      .from(
        UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
        AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
        Instant.ofEpochSecond(1),
        Instant.ofEpochSecond(21),
        true);

    AcquiredChannelEnvironmentIssueBoolean firstAcei = AcquiredChannelEnvironmentIssueBoolean.from(
      UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
      AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
      Instant.ofEpochSecond(1),
      Instant.ofEpochSecond(11),
      true);

    AcquiredChannelEnvironmentIssueBoolean secondAcei = AcquiredChannelEnvironmentIssueBoolean
      .from(
        UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
        AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
        Instant.ofEpochSecond(11),
        Instant.ofEpochSecond(21),
        false);

    assertTrue(enclosingAcei.encloses(firstAcei));
    assertTrue(enclosingAcei.encloses(secondAcei));
    assertFalse(firstAcei.encloses(secondAcei));
  }

  @Test
  void testSerializeDeserializeAcei() throws Exception {

    AcquiredChannelEnvironmentIssueBoolean aceiBoolean1 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
        AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
        Instant.ofEpochSecond(1),
        Instant.ofEpochSecond(11),
        true);
    AcquiredChannelEnvironmentIssueBoolean aceiBoolean2 = AcquiredChannelEnvironmentIssueBoolean
      .from(
        UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
        AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
        Instant.ofEpochSecond(11),
        Instant.ofEpochSecond(21),
        false);

    AcquiredChannelEnvironmentIssueAnalog aceiAnalog1 = AcquiredChannelEnvironmentIssueAnalog.from(
      UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
      AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
      Instant.ofEpochSecond(1),
      Instant.ofEpochSecond(11),
      15.123);
    AcquiredChannelEnvironmentIssueAnalog aceiAnalog2 = AcquiredChannelEnvironmentIssueAnalog.from(
      UtilsTestFixtures.PROCESSING_CHANNEL_1_NAME,
      AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
      Instant.ofEpochSecond(11),
      Instant.ofEpochSecond(21),
      16.456);

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    String actualJson = objectMapper.writeValueAsString(aceiBoolean1);
    AcquiredChannelEnvironmentIssue<?> actualAcei = objectMapper
      .readValue(actualJson, AcquiredChannelEnvironmentIssue.class);
    assertTrue(actualAcei instanceof AcquiredChannelEnvironmentIssueBoolean);
    assertEquals(aceiBoolean1, actualAcei);

    AcquiredChannelEnvironmentIssueBoolean actualBoolean = objectMapper
      .readValue(actualJson, AcquiredChannelEnvironmentIssueBoolean.class);
    assertEquals(aceiBoolean1, actualBoolean);

    actualJson = objectMapper.writeValueAsString(aceiAnalog1);
    actualAcei = objectMapper.readValue(actualJson, AcquiredChannelEnvironmentIssue.class);
    assertTrue(actualAcei instanceof AcquiredChannelEnvironmentIssueAnalog);
    assertEquals(aceiAnalog1, actualAcei);

    AcquiredChannelEnvironmentIssueAnalog actualAnalog = objectMapper
      .readValue(actualJson, AcquiredChannelEnvironmentIssueAnalog.class);
    assertEquals(aceiAnalog1, actualAnalog);

    // List of Boolean SOH.
    List<AcquiredChannelEnvironmentIssueBoolean> aceiBooleans = List.of(aceiBoolean1, aceiBoolean2);
    List<AcquiredChannelEnvironmentIssueAnalog> aceiAnalogs = List.of(aceiAnalog1, aceiAnalog2);
    List<AcquiredChannelEnvironmentIssue<?>> aceis = List
      .of(aceiBoolean1, aceiBoolean2, aceiAnalog1, aceiAnalog2);

    actualJson = objectMapper.writeValueAsString(aceiBooleans);
    List<AcquiredChannelEnvironmentIssueBoolean> actualBooleans = objectMapper.readValue(actualJson,
      new TypeReference<>() {
      });
    assertEquals(aceiBooleans, actualBooleans);

    actualJson = objectMapper.writeValueAsString(aceiAnalogs);
    List<AcquiredChannelEnvironmentIssueAnalog> actualAnalogs = objectMapper.readValue(actualJson,
      new TypeReference<>() {
      });
    assertEquals(aceiAnalogs, actualAnalogs);

    actualJson = objectMapper.writeValueAsString(aceis);
    List<AcquiredChannelEnvironmentIssue<?>> actualAceis = objectMapper.readValue(actualJson,
      new TypeReference<>() {
      });
    assertEquals(aceis, actualAceis);
  }
}
