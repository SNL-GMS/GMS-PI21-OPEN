package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceResponseTest {

  @Test
  void testSerialization() throws Exception {
    TestUtilities
      .testSerialization(StationReferenceTestFixtures.REFERENCE_RESPONSE, ReferenceResponse.class);
  }

  @Test
  void testReferenceResponseCreate() {
    ReferenceResponse response = ReferenceResponse.builder()
      .setChannelName(StationReferenceTestFixtures.CHANNEL_NAME)
      .setActualTime(StationReferenceTestFixtures.ACTUAL_TIME)
      .setSystemTime(StationReferenceTestFixtures.SYSTEM_TIME)
      .setComment(StationReferenceTestFixtures.COMMENT)
      .setSourceResponse(StationReferenceTestFixtures.REFERENCE_SOURCE_RESPONSE)
      .setReferenceCalibration(StationReferenceTestFixtures.REF_CALIBRATION_BHE_V_1)
      .setFapResponse(StationReferenceTestFixtures.FAP)
      .build();

    assertEquals(StationReferenceTestFixtures.CHANNEL_NAME, response.getChannelName());
    assertEquals(StationReferenceTestFixtures.ACTUAL_TIME, response.getActualTime());
    assertEquals(StationReferenceTestFixtures.SYSTEM_TIME, response.getSystemTime());
    assertEquals(StationReferenceTestFixtures.COMMENT, response.getComment());
    assertEquals(Optional.of(StationReferenceTestFixtures.REFERENCE_SOURCE_RESPONSE), response.getSourceResponse());
    assertEquals(StationReferenceTestFixtures.SYSTEM_TIME, response.getSystemTime());
    assertEquals(StationReferenceTestFixtures.REF_CALIBRATION_BHE_V_1, response.getReferenceCalibration());
    assertEquals(StationReferenceTestFixtures.FAP, response.getFapResponse());
  }
}

