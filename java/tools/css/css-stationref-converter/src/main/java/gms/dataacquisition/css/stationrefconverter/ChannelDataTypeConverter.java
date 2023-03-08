package gms.dataacquisition.css.stationrefconverter;


import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.DIAGNOSTIC_SOH;
import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.HYDROACOUSTIC;
import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.INFRASOUND;
import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.SEISMIC;
import static gms.shared.frameworks.osd.coi.channel.ChannelDataType.WEATHER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.ACCELEROMETER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.ELECTRONIC_TEST_POINT;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.GRAVIMETER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.HUMIDITY;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.LOW_GAIN_SEISMOMETER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.MASS_POSITION_SEISMOMETER;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.PRESSURE;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.TEMPERATURE;
import static gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType.WIND;
import static java.util.Map.entry;

class ChannelDataTypeConverter {
  private static final Map<ChannelInstrumentType, ChannelDataType> instrumentToDataType = Map
    .ofEntries(
      entry(HIGH_GAIN_SEISMOMETER, SEISMIC),
      entry(LOW_GAIN_SEISMOMETER, SEISMIC),
      entry(GRAVIMETER, SEISMIC),
      entry(MASS_POSITION_SEISMOMETER, SEISMIC),
      entry(ACCELEROMETER, SEISMIC),
      entry(ELECTRONIC_TEST_POINT, DIAGNOSTIC_SOH),
      entry(WIND, WEATHER)
    );

  private static final Set<ChannelInstrumentType> orientedWeatherInstruments = Set.of(
    TEMPERATURE, PRESSURE, HUMIDITY
  );

  private static final Map<Character, ChannelDataType> weatherOrientationToDataType = Map
    .ofEntries(
      entry('A', WEATHER),
      entry('O', WEATHER),
      entry('I', DIAGNOSTIC_SOH),
      entry('D', DIAGNOSTIC_SOH),
      entry('U', DIAGNOSTIC_SOH),
      entry('F', INFRASOUND),
      entry('H', HYDROACOUSTIC)
    );

  /**
   * Retrieves a {@link ChannelDataType} for the instrument/orientation pair.
   *
   * @param instrumentType Type of channel instrument
   * @param orientationCode Code for the orientation of the channel
   * @return The data type of the channel, or {@link Optional#empty} if a type could not be
   * successfully retrieved.
   */
  static Optional<ChannelDataType> getChannelDataType(ChannelInstrumentType instrumentType,
    char orientationCode) {

    return isOrientedWeatherInstrument(instrumentType)
      ? getWeatherDataTypeForOrientationCode(orientationCode)
      : getDataTypeForInstrument(instrumentType);
  }

  private static boolean isOrientedWeatherInstrument(ChannelInstrumentType instrumentType) {
    return orientedWeatherInstruments.contains(instrumentType);
  }

  private static Optional<ChannelDataType> getDataTypeForInstrument(
    ChannelInstrumentType instrumentType) {
    return Optional.ofNullable(instrumentToDataType.get(instrumentType));
  }

  private static Optional<ChannelDataType> getWeatherDataTypeForOrientationCode(char orientationCode) {
    return Optional.ofNullable(weatherOrientationToDataType.get(orientationCode));
  }

  private ChannelDataTypeConverter() {
    // private default constructor to hide implicit public one.
  }
}
