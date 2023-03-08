package gms.dataacquisition.css.stationrefconverter;

import org.junit.jupiter.api.Test;

import static gms.dataacquisition.css.stationrefconverter.ChannelDataTypeConverter.getChannelDataType;
import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.INFRASOUND;
import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.SEISMIC;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.PRESSURE;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.TEMPERATURE;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ChannelDataTypeConverterTest {

  @Test
  void testKnownInstrumentReturnsCorrectType() {
    getChannelDataType(HIGH_GAIN_SEISMOMETER, 'Z')
      .ifPresentOrElse(dataType -> assertEquals(SEISMIC, dataType),
        () -> fail("Test should have returned nonEmpty Optional ChannelDataType"));
  }

  @Test
  void testKnownWeatherInstrumentReturnsCorrectType() {
    getChannelDataType(PRESSURE, 'F')
      .ifPresentOrElse(dataType -> assertEquals(INFRASOUND, dataType),
        () -> fail("Test should have returned nonEmpty Optional ChannelDataType"));
  }

  @Test
  void testUnknownInstrumentReturnsEmpty() {
    assertTrue(getChannelDataType(UNKNOWN, 'Z').isEmpty());
  }

  @Test
  void testUnknownWeatherCodeReturnsEmpty() {
    assertTrue(getChannelDataType(TEMPERATURE, 'Z').isEmpty());
  }
}