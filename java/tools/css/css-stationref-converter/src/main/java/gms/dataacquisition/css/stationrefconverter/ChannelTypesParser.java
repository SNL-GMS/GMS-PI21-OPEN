package gms.dataacquisition.css.stationrefconverter;

import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public class ChannelTypesParser {

  private static final Logger logger = LoggerFactory.getLogger(ChannelTypesParser.class);

  private static final Map<Character, ChannelTypes> weatherTypesByCode = Map.ofEntries(
    entry('S', ChannelTypes.builder()
      .setInstrumentType(ChannelInstrumentType.WIND)
      .setOrientationCode('S')
      .setDataType(ChannelDataType.WEATHER)
      .build()),
    entry('D', ChannelTypes.builder()
      .setInstrumentType(ChannelInstrumentType.WIND)
      .setOrientationCode('D')
      .setDataType(ChannelDataType.WEATHER)
      .build()),
    entry('T', ChannelTypes.builder()
      .setInstrumentType(ChannelInstrumentType.TEMPERATURE)
      .setOrientationCode('T')
      .setDataType(ChannelDataType.WEATHER)
      .build())
  );

  /**
   * Attempts to parse out all channel types (band, instrument. etc.) from a given channel string.
   * Manages such edge cases as two-character versus three-character channels, and the weather channels.
   *
   * @param chan two or three character combination of channel codes from a sitechan record (e.g. SHZ)
   * @return All parsed channel types, or {@link Optional#empty} if there was any parsing issues.
   */
  static Optional<ChannelTypes> parseChannelTypes(String chan) {
    char[] channelCodes = chan.trim().toUpperCase(Locale.ENGLISH).toCharArray();
    int numCodes = channelCodes.length;

    final Optional<ChannelTypes> channelTypes;
    if (numCodes == 2) {
      channelTypes = handleTwoCharacterChannel(channelCodes[0], channelCodes[1]);
    } else if (numCodes == 3) {
      channelTypes = handleThreeCharacterChannel(channelCodes[0], channelCodes[1], channelCodes[2]);
    } else {
      logger.error("Unknown channel {} encountered: expected 2 or 3 character codes", chan);
      channelTypes = Optional.empty();
    }
    return channelTypes;
  }

  private static Optional<ChannelTypes> handleTwoCharacterChannel(char firstCode, char secondCode) {
    if ('W' == Character.toUpperCase(firstCode)) {
      return getWeatherTypes(secondCode);
    } else {
      return Optional.of(parseSeismicTypes(firstCode, secondCode));
    }
  }

  private static Optional<ChannelTypes> getWeatherTypes(Character weatherCode) {
    return Optional.ofNullable(weatherTypesByCode.get(Character.toUpperCase(weatherCode)));
  }

  private static ChannelTypes parseSeismicTypes(char bandCode, char orientationCode) {
    return ChannelTypes.builder()
      .setBandType(ChannelBandType.fromCode(bandCode))
      .setOrientationType(ChannelOrientationType.fromCode(orientationCode))
      .setOrientationCode(orientationCode)
      .setDataType(ChannelDataType.SEISMIC)
      .build();
  }

  private static Optional<ChannelTypes> handleThreeCharacterChannel(char bandCode,
    char instrumentCode,
    char orientationCode) {
    var bandType = ChannelBandType.fromCode(bandCode);
    var instrumentType = ChannelInstrumentType.fromCode(instrumentCode);
    var dataTypeOptional = ChannelDataTypeConverter
      .getChannelDataType(instrumentType, orientationCode);

    if (dataTypeOptional.isEmpty()) {
      logger.warn("No ChannelDataType found for instrument {}, orientation code {}",
        instrumentType, orientationCode);
      return Optional.empty();
    }

    var dataType = dataTypeOptional.get();
    var channelTypesBuilder = ChannelTypes.builder()
      .setBandType(bandType)
      .setInstrumentType(instrumentType)
      .setOrientationCode(orientationCode)
      .setDataType(dataType);

    if (ChannelDataType.SEISMIC.equals(dataType)) {
      channelTypesBuilder.setOrientationType(ChannelOrientationType.fromCode(orientationCode));
    }

    return Optional.of(channelTypesBuilder.build());
  }

  private ChannelTypesParser() {
    //private constructor for static factory
  }
}
