package gms.shared.stationdefinition.coi.channel;


import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static gms.shared.stationdefinition.coi.channel.ChannelDataType.DIAGNOSTIC_SOH;
import static gms.shared.stationdefinition.coi.channel.ChannelDataType.HYDROACOUSTIC;
import static gms.shared.stationdefinition.coi.channel.ChannelDataType.INFRASOUND;
import static gms.shared.stationdefinition.coi.channel.ChannelDataType.SEISMIC;
import static gms.shared.stationdefinition.coi.channel.ChannelDataType.WEATHER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.ACCELEROMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.ELECTRONIC_TEST_POINT;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.GEOPHONE;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.GRAVIMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.HUMIDITY;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.LOW_GAIN_SEISMOMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.MASS_POSITION_SEISMOMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.PRESSURE;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.TEMPERATURE;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.WIND;
import static java.util.Map.entry;

class ChannelDataTypeConverter {
  private static final EnumMap<ChannelInstrumentType, ChannelDataType> instrumentToDataType
    = new EnumMap<>(ChannelInstrumentType.class);

  static {
    instrumentToDataType.put(HIGH_GAIN_SEISMOMETER, SEISMIC);
    instrumentToDataType.put(LOW_GAIN_SEISMOMETER, SEISMIC);
    instrumentToDataType.put(GEOPHONE, SEISMIC);
    instrumentToDataType.put(GRAVIMETER, SEISMIC);
    instrumentToDataType.put(MASS_POSITION_SEISMOMETER, SEISMIC);
    instrumentToDataType.put(ACCELEROMETER, SEISMIC);
    instrumentToDataType.put(ELECTRONIC_TEST_POINT, DIAGNOSTIC_SOH);
    instrumentToDataType.put(WIND, WEATHER);
  }

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
      entry('H', HYDROACOUSTIC),
      entry('1', WEATHER),
      entry('2', WEATHER),
      entry('3', WEATHER),
      entry('4', WEATHER)


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
