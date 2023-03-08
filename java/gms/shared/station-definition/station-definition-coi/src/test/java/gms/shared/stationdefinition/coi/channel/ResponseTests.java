package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class ResponseTests {

  private final static Logger logger = LoggerFactory.getLogger(ResponseTests.class);
  private final static ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();
  private UUID testUUID = UUID.nameUUIDFromBytes("test".getBytes());

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(UtilsTestFixtures.RESPONSE_1, Response.class);
  }

  @Test
  void testSerialization_entityReference() {
    TestUtilities.assertSerializes(Response.createEntityReference(testUUID), Response.class);

  }

  @Test
  void testResponseCreate() {
    final Instant now = Instant.now();
    Response response = Response.builder()
      .setId(UUID.nameUUIDFromBytes(UtilsTestFixtures.CHANNEL_NAME.getBytes()))
      .setEffectiveAt(now)
      .setData(Response.Data.builder()
        .setCalibration(UtilsTestFixtures.calibration)
        .setFapResponse(UtilsTestFixtures.fapResponse)
        .setEffectiveUntil(now.plusSeconds(10))
        .build())
      .build();

    assertEquals(UUID.nameUUIDFromBytes(UtilsTestFixtures.CHANNEL_NAME.getBytes()),
      response.getId());
    response.getEffectiveAt()
      .ifPresentOrElse(instant -> assertEquals(now, instant),
        () -> fail());
    Assertions.assertEquals(UtilsTestFixtures.calibration, response.getCalibration());
    Assertions.assertEquals(UtilsTestFixtures.fapResponse, response.getFapResponse());
    Assertions.assertEquals(now.plusSeconds(10), response.getEffectiveUntil().get());
  }

  @Test
  void testResponseCompare() {
    final Instant now = Instant.now();
    Response responseSame = Response.builder()
      .setId(UtilsTestFixtures.RESPONSE_1.getId())
      .setEffectiveAt(UtilsTestFixtures.RESPONSE_1.getEffectiveAt())
      .setData(Response.Data.builder()
        .setCalibration(UtilsTestFixtures.RESPONSE_1.getCalibration())
        .setFapResponse(UtilsTestFixtures.RESPONSE_1.getFapResponse())
        .build())
      .build();
    Response responseDifferent1 = Response.builder()
      .setId(UUID.nameUUIDFromBytes("different".getBytes()))
      .setEffectiveAt(UtilsTestFixtures.RESPONSE_1.getEffectiveAt())
      .setData(Response.Data.builder()
        .setCalibration(UtilsTestFixtures.RESPONSE_1.getCalibration())
        .setFapResponse(UtilsTestFixtures.RESPONSE_1.getFapResponse())
        .build())
      .build();
    Response responseDifferent2 = Response.builder()
      .setId(UtilsTestFixtures.RESPONSE_1.getId())
      .setEffectiveAt(now)
      .setData(Response.Data.builder()
        .setCalibration(UtilsTestFixtures.RESPONSE_1.getCalibration())
        .setFapResponse(UtilsTestFixtures.RESPONSE_1.getFapResponse())
        .build())
      .build();
    Assertions.assertEquals(0, UtilsTestFixtures.RESPONSE_1.compareTo(responseSame));
    Assertions.assertNotEquals(0, UtilsTestFixtures.RESPONSE_1.compareTo(responseDifferent1));
    Assertions.assertNotEquals(0, UtilsTestFixtures.RESPONSE_1.compareTo(responseDifferent2));
  }

  @Test
  void testResponse_CreateEntityReference_present() {
    Response response = getResponseWithOnlyId(testUUID);
    assertFalse(response.isPresent());
  }

  @Test
  void testResponse_createEntityReference_serializeToAndFrom()
    throws JsonProcessingException {

    final String json = mapper.writeValueAsString(UtilsTestFixtures.RESPONSE_1);
    logger.info("json serialized response: {}", json);

    final Response deserialized = mapper.readValue(json, Response.class);
    assertEquals(UtilsTestFixtures.RESPONSE_1, deserialized);
  }

  @Test
  void testResponse_CreateEntityReference() {

    var uuid = UUID.randomUUID();
    var expected = getResponseWithOnlyId(uuid);
    var response = Response.createEntityReference(expected);
    assertEquals(expected, response);
  }

  @Test
  void testResponse_CreateVersionReference() {

    var uuid = UUID.randomUUID();
    var instant = Instant.parse("2019-06-06T17:26:00Z");
    var expected = getResponseWithIdTime(uuid, instant);
    var response = Response.createVersionReference(expected);
    assertEquals(expected, response);
  }

  private Response getResponseWithOnlyId(UUID id) {
    return Response.createEntityReference(id);
  }

  private Response getResponseWithIdTime(UUID id, Instant time) {
    return Response.createVersionReference(id, time);
  }
}
