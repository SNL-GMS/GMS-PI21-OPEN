package gms.shared.stationdefinition.coi.channel;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

  private static final Set<String> NON_CONVENTIONAL_INFRASOUND_CHANNELS = Set.of("LDA", "BDA");

  public static Set<String> getNonCoventionalInfrasoundChannels() {
    return NON_CONVENTIONAL_INFRASOUND_CHANNELS;
  }

  /**
   * Attempts to parse out all channel types (band, instrument. etc.) from a given channel string.
   * Manages such edge cases as two-character versus three-character channels, and the weather channels.
   *
   * @param chan two or three character combination of channel codes from a sitechan record (e.g. SHZ)
   * @return All parsed channel types, or {@link Optional#empty} if there was any parsing issues.
   */
  public static Optional<ChannelTypes> parseChannelTypes(String chan) {

    Objects.requireNonNull(chan, "Channel string must not be null.");
    Preconditions.checkState(!chan.isEmpty(), "Channel string must not be empty.");

    char[] channelCodes = chan.trim().toUpperCase(Locale.ENGLISH).toCharArray();
    int numCodes = channelCodes.length;

    final Optional<ChannelTypes> channelTypes;
    if (numCodes == 2) {
      channelTypes = handleTwoCharacterChannel(channelCodes[0], channelCodes[1]);
    } else if (numCodes == 3) {
      channelTypes = handleThreeCharacterChannel(channelCodes[0], channelCodes[1], channelCodes[2]);
    } else {
      logger.error("Invalid channel encountered: {}. Expected 2 or 3 character codes", chan);
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
      //TODO replace fromCode(orientationCode) with something less brittle- dbc 4/15/2021
      .setOrientationType(ChannelOrientationType.fromCode(orientationCode))
      .setOrientationCode(orientationCode)
      .setDataType(ChannelDataType.SEISMIC)
      .build();
  }

  private static Optional<ChannelTypes> handleThreeCharacterChannel(char bandCode,
    char instrumentCode,
    char orientationCode) {

    char corectedOrientationCode;

    var chan = new StringBuilder().append(bandCode).append(instrumentCode).append(orientationCode).toString();
    if (NON_CONVENTIONAL_INFRASOUND_CHANNELS.contains(chan)) {
      //orientation code is A for these channels, but should be F by convention, so it is set to F here
      corectedOrientationCode = 'F';
    } else {
      corectedOrientationCode = orientationCode;
    }


    var bandType = ChannelBandType.fromCode(bandCode);
    var instrumentType = ChannelInstrumentType.fromCode(instrumentCode);
    Optional<ChannelDataType> dataTypeOptional = ChannelDataTypeConverter
      .getChannelDataType(instrumentType, corectedOrientationCode);

    if (dataTypeOptional.isEmpty()) {
      logger.warn("No ChannelDataType found for instrument {}, orientation code {}",
        instrumentType, corectedOrientationCode);
      return Optional.empty();
    }

    var dataType = dataTypeOptional.get();
    var typesBuilder = ChannelTypes.builder()
      .setBandType(bandType)
      .setInstrumentType(instrumentType)
      .setOrientationCode(corectedOrientationCode)
      .setDataType(dataType);

    final var orientationType =
      ChannelOrientationType.fromCode(corectedOrientationCode, instrumentCode);


    typesBuilder.setOrientationType(orientationType);

    return Optional.of(typesBuilder.build());
  }

  private ChannelTypesParser() {
    //private constructor for static factory
  }
}
