package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceSensorTest {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.REFERENCE_SENSOR,
      ReferenceSensor.class);
  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  void testReferenceSensorCreate() {
    ReferenceSensor sensor = ReferenceSensor.builder()
      .setChannelName(StationReferenceTestFixtures.REFERENCE_CHANNEL.getName())
      .setInstrumentManufacturer(StationReferenceTestFixtures.INSTRUMENT_MANUFACTURER)
      .setInstrumentModel(StationReferenceTestFixtures.INSTRUMENT_MODEL)
      .setSerialNumber(StationReferenceTestFixtures.SERIAL_NUMBER)
      .setNumberOfComponents(StationReferenceTestFixtures.NUMBER_OF_COMPONENTS)
      .setCornerPeriod(StationReferenceTestFixtures.CORNER_PERIOD)
      .setLowPassband(StationReferenceTestFixtures.LOW_PASSBAND)
      .setHighPassband(StationReferenceTestFixtures.HIGH_PASSBAND)
      .setActualTime(StationReferenceTestFixtures.ACTUAL_TIME)
      .setSystemTime(StationReferenceTestFixtures.SYSTEM_TIME)
      .setInformationSource(StationReferenceTestFixtures.INFORMATION_SOURCE)
      .setComment(StationReferenceTestFixtures.COMMENT)
      .build();
    final UUID expectedId = UUID.nameUUIDFromBytes(
      (sensor.getChannelName() + sensor.getInstrumentManufacturer()
        + sensor.getInstrumentModel() + sensor.getSerialNumber()
        + sensor.getNumberOfComponents() + sensor.getCornerPeriod()
        + sensor.getLowPassband() + sensor.getHighPassband()
        + sensor.getActualTime() + sensor.getSystemTime())
        .getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, sensor.getId());
    assertEquals(StationReferenceTestFixtures.REFERENCE_CHANNEL.getName(), sensor.getChannelName());
    assertEquals(StationReferenceTestFixtures.INSTRUMENT_MANUFACTURER,
      sensor.getInstrumentManufacturer());
    assertEquals(StationReferenceTestFixtures.INSTRUMENT_MODEL, sensor.getInstrumentModel());
    assertEquals(StationReferenceTestFixtures.SERIAL_NUMBER, sensor.getSerialNumber());
    assertEquals(StationReferenceTestFixtures.NUMBER_OF_COMPONENTS, sensor.getNumberOfComponents());
    assertEquals(StationReferenceTestFixtures.CORNER_PERIOD, sensor.getCornerPeriod(),
      StationReferenceTestFixtures.PRECISION);
    assertEquals(StationReferenceTestFixtures.LOW_PASSBAND, sensor.getLowPassband(),
      StationReferenceTestFixtures.PRECISION);
    assertEquals(StationReferenceTestFixtures.HIGH_PASSBAND, sensor.getHighPassband(),
      StationReferenceTestFixtures.PRECISION);
    assertEquals(StationReferenceTestFixtures.ACTUAL_TIME, sensor.getActualTime());
    assertEquals(StationReferenceTestFixtures.SYSTEM_TIME, sensor.getSystemTime());
    assertEquals(StationReferenceTestFixtures.INFORMATION_SOURCE, sensor.getInformationSource());
    assertEquals(StationReferenceTestFixtures.COMMENT, sensor.getComment());
  }

}
