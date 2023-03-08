package gms.shared.stationdefinition.coi.station;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationIdTest {
  @Test
  void testEmptyEffectiveTime() {
    StationId stationId = StationId.builder()
      .setName("some station")
      .build();
    assertTrue(stationId.getEffectiveAt().isEmpty());
  }

  @Test
  void testEmptyNameException() {
    StationId.Builder builder = StationId.builder()
      .setEffectiveAt(Instant.EPOCH);

    assertThrows(IllegalStateException.class, builder::build);
  }

  @Test
  void testSerialization() {
    StationId stationId = StationId.builder()
      .setEffectiveAt(Instant.EPOCH)
      .setName("some station")
      .build();
    TestUtilities.assertSerializes(stationId, StationId.class);
  }

  @Test
  void testCreateEntityReference() {
    StationId stationId = StationId.createEntityReference("some station");
    assertEquals("some station", stationId.getName());
    assertTrue(stationId.getEffectiveAt().isEmpty());
  }

  @Test
  void testCreateVersionReference() {
    StationId stationId = StationId.createVersionReference("some station", Instant.EPOCH);
    assertEquals("some station", stationId.getName());
    assertEquals(Instant.EPOCH, stationId.getEffectiveAt().orElseThrow());
  }

  @Test
  void testToEntityReference() {
    StationId stationId = StationId.builder()
      .setName("someStation")
      .setEffectiveAt(Instant.EPOCH)
      .build();
    Station station = stationId.toStationEntityReference();

    assertFalse(station.isPresent());
    assertFalse(station.getEffectiveAt().isPresent());
  }

  @Test
  void testToVersionReference() {
    StationId stationId = StationId.builder()
      .setName("someStation")
      .setEffectiveAt(Instant.EPOCH)
      .build();
    Station station = stationId.toStationVersionReference();

    assertFalse(station.isPresent());
    assertTrue(station.getEffectiveAt().isPresent());
  }
}
