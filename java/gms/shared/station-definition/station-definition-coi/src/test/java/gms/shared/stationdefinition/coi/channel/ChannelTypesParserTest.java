package gms.shared.stationdefinition.coi.channel;

import gms.shared.stationdefinition.coi.channel.ChannelTypes.Builder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.coi.channel.ChannelBandType.BROADBAND;
import static gms.shared.stationdefinition.coi.channel.ChannelBandType.EXTREMELY_SHORT_PERIOD;
import static gms.shared.stationdefinition.coi.channel.ChannelBandType.HIGH_BROADBAND;
import static gms.shared.stationdefinition.coi.channel.ChannelBandType.LONG_PERIOD;
import static gms.shared.stationdefinition.coi.channel.ChannelBandType.MID_PERIOD;
import static gms.shared.stationdefinition.coi.channel.ChannelBandType.SHORT_PERIOD;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.ACCELEROMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.GEOPHONE;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.HIGH_GAIN_SEISMOMETER;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.PRESSURE;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.TEMPERATURE;
import static gms.shared.stationdefinition.coi.channel.ChannelInstrumentType.WIND;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.CABINET_SOURCES_1;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.CABINET_SOURCES_2;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.CABINET_SOURCES_3;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.CABINET_SOURCES_4;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.DOWN_HOLE;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.EAST_WEST;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.HYDROPHONE;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.INFRASOUND;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.INSIDE;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.NORTH_SOUTH;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.OPTIONAL_U;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.OPTIONAL_V;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.ORTHOGONAL_1;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.OUTSIDE;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.RADIAL;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.TRANSVERSE;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.UNDERGROUND;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.VERTICAL;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.WIND_DIRECTION_VECTOR;
import static gms.shared.stationdefinition.coi.channel.ChannelOrientationType.WIND_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ChannelTypesParserTest {

  @ParameterizedTest
  @MethodSource("parseChannelTypesArguments")
  void parseChannelTypes(String channelCode, Optional<ChannelBandType> bandType,
    Optional<ChannelInstrumentType> instrumentType,
    Optional<ChannelOrientationType> orientationType) {
    Optional<ChannelTypes> expected = bandType.flatMap(channelBandType ->
      orientationType.flatMap(channelOrientationType ->
        instrumentType.map(channelInstrumentType ->
            getChannelTypes(channelBandType, channelInstrumentType, channelOrientationType))
          .orElseGet(() ->
            getChannelTypes(channelBandType, channelOrientationType))
      ).or(Optional::empty)
    ).or(Optional::empty);

    final Optional<ChannelTypes> result = ChannelTypesParser.parseChannelTypes(channelCode);

    assertNotNull(result);
    assertEquals(expected, result);
    if (result.isPresent()) {
      assertNotNull(result.get().getBandType());
      assertNotEquals(ChannelBandType.UNKNOWN, result.get().getBandType());
      assertNotNull(result.get().getDataType());
      assertNotNull(result.get().getInstrumentType());
      if (channelCode.length() >= 3) {
        assertNotEquals(ChannelInstrumentType.UNKNOWN, result.get().getInstrumentType());
      }
      assertNotNull(result.get().getOrientationType());
      assertNotEquals(ChannelOrientationType.UNKNOWN, result.get().getOrientationType());
      assertEquals(result.get().getOrientationCode(), result.get().getOrientationType().getCode());

      bandType.ifPresent(channelBandType -> {
        assertEquals(channelBandType, result.get().getBandType());
        orientationType.ifPresent(channelOrientationType -> {
          assertEquals(channelOrientationType, result.get().getOrientationType());
          instrumentType.ifPresent(channelInstrumentType ->
            assertEquals(channelInstrumentType, result.get().getInstrumentType()));
        });
      });
    }
  }

  private static Stream<Arguments> parseChannelTypesArguments() {
    return Stream.of(
      //3 Letter Codes
      arguments("EDH", Optional.of(EXTREMELY_SHORT_PERIOD), Optional.of(PRESSURE),
        Optional.of(HYDROPHONE)),
      arguments("BDF", Optional.of(BROADBAND), Optional.of(PRESSURE), Optional.of(INFRASOUND)),
      arguments("BKO", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(OUTSIDE)),
      arguments("BKI", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(INSIDE)),
      arguments("BKD", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(DOWN_HOLE)),
      arguments("BK1", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(CABINET_SOURCES_1)),
      arguments("BK2", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(CABINET_SOURCES_2)),
      arguments("BK3", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(CABINET_SOURCES_3)),
      arguments("BK4", Optional.of(BROADBAND), Optional.of(TEMPERATURE), Optional.of(CABINET_SOURCES_4)),
      arguments("BWD", Optional.of(BROADBAND), Optional.of(WIND), Optional.of(WIND_DIRECTION_VECTOR)),
      arguments("BWS", Optional.of(BROADBAND), Optional.of(WIND), Optional.of(WIND_SPEED)),
      arguments("LKO", Optional.of(LONG_PERIOD), Optional.of(TEMPERATURE), Optional.of(OUTSIDE)),
      arguments("MKO", Optional.of(MID_PERIOD), Optional.of(TEMPERATURE), Optional.of(OUTSIDE)),
      arguments("BPN", Optional.of(BROADBAND), Optional.of(GEOPHONE), Optional.of(NORTH_SOUTH)),
      arguments("BPE", Optional.of(BROADBAND), Optional.of(GEOPHONE), Optional.of(EAST_WEST)),
      arguments("BPZ", Optional.of(BROADBAND), Optional.of(GEOPHONE), Optional.of(VERTICAL)),
      arguments("BHN", Optional.of(BROADBAND), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(NORTH_SOUTH)),
      arguments("BHE", Optional.of(BROADBAND), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(EAST_WEST)),
      arguments("BHZ", Optional.of(BROADBAND), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(VERTICAL)),
      arguments("BH1", Optional.of(BROADBAND), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(ORTHOGONAL_1)),
      arguments("SHZ", Optional.of(SHORT_PERIOD), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(VERTICAL)),
      arguments("HHN", Optional.of(HIGH_BROADBAND), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(NORTH_SOUTH)),
      arguments("HNN", Optional.of(HIGH_BROADBAND), Optional.of(ACCELEROMETER),
        Optional.of(NORTH_SOUTH)),
      arguments("LDA", Optional.of(LONG_PERIOD), Optional.of(PRESSURE), Optional.of(INFRASOUND)),
      arguments("LDU", Optional.of(LONG_PERIOD), Optional.of(PRESSURE), Optional.of(UNDERGROUND)),
      arguments("LHU", Optional.of(LONG_PERIOD), Optional.of(HIGH_GAIN_SEISMOMETER),
        Optional.of(OPTIONAL_U)),
      //2 Letter Codes
      arguments("SO", Optional.of(SHORT_PERIOD), Optional.empty(), Optional.of(OUTSIDE)),
      arguments("MT", Optional.of(MID_PERIOD), Optional.empty(), Optional.of(TRANSVERSE)),
      arguments("MR", Optional.of(MID_PERIOD), Optional.empty(), Optional.of(RADIAL)),
      arguments("MN", Optional.of(MID_PERIOD), Optional.empty(), Optional.of(NORTH_SOUTH)),
      arguments("BN", Optional.of(BROADBAND), Optional.empty(), Optional.of(NORTH_SOUTH)),
      arguments("BE", Optional.of(BROADBAND), Optional.empty(), Optional.of(EAST_WEST)),
      arguments("SE", Optional.of(SHORT_PERIOD), Optional.empty(), Optional.of(EAST_WEST)),
      arguments("SU", Optional.of(SHORT_PERIOD), Optional.empty(), Optional.of(OPTIONAL_U)),
      arguments("SV", Optional.of(SHORT_PERIOD), Optional.empty(), Optional.of(OPTIONAL_V)),
      arguments("S1", Optional.of(SHORT_PERIOD), Optional.empty(), Optional.of(ORTHOGONAL_1)),
      arguments("SZ", Optional.of(SHORT_PERIOD), Optional.empty(), Optional.of(VERTICAL)),
      arguments("EE", Optional.of(EXTREMELY_SHORT_PERIOD), Optional.empty(),
        Optional.of(EAST_WEST)),
      arguments("EZ", Optional.of(EXTREMELY_SHORT_PERIOD), Optional.empty(),
        Optional.of(VERTICAL)),
      //1 Letter Codes
      arguments("B", Optional.empty(), Optional.empty(), Optional.empty()),
      arguments("D", Optional.empty(), Optional.empty(), Optional.empty()),
      arguments("K", Optional.empty(), Optional.empty(), Optional.empty()),
      arguments("L", Optional.empty(), Optional.empty(), Optional.empty()),
      arguments("M", Optional.empty(), Optional.empty(), Optional.empty()),
      arguments("S", Optional.empty(), Optional.empty(), Optional.empty()),
      arguments("U", Optional.empty(), Optional.empty(), Optional.empty())
    );
  }

  private static Optional<ChannelTypes> getChannelTypes(ChannelBandType bandType,
    ChannelInstrumentType instrumentType,
    ChannelOrientationType orientationType) {
    return getChannelTypes(bandType, instrumentType, orientationType.getCode());
  }

  private static Optional<ChannelTypes> getChannelTypes(ChannelBandType bandType,
    ChannelInstrumentType instrumentType,
    char orientationCode) {
    ChannelOrientationType orientationType = ChannelOrientationType
      .fromCode(orientationCode, instrumentType.getCode());
    if (orientationType == ChannelOrientationType.UNKNOWN) {
      orientationType = ChannelOrientationType.fromCode(orientationCode);
    }

    final Builder builder = ChannelTypes.builder()
      .setBandType(bandType)
      .setInstrumentType(instrumentType)
      .setOrientationCode(orientationCode)
      .setOrientationType(orientationType)
      .setDataType(ChannelDataTypeConverter
        .getChannelDataType(instrumentType, orientationCode)
        .orElse(ChannelDataType.DIAGNOSTIC_SOH));

    return Optional.of(builder.build());
  }

  private static Optional<ChannelTypes> getChannelTypes(ChannelBandType bandType,
    ChannelOrientationType orientationType) {
    return getChannelTypes(bandType, orientationType.getCode());
  }

  private static Optional<ChannelTypes> getChannelTypes(ChannelBandType bandType,
    char orientationCode) {
    final ChannelOrientationType orientationType = ChannelOrientationType
      .fromCode(orientationCode);

    final Builder builder = ChannelTypes.builder()
      .setBandType(bandType)
      .setOrientationCode(orientationType.getCode())
      .setOrientationType(orientationType)
      .setDataType(ChannelDataType.SEISMIC);

    return Optional.of(builder.build());
  }

}