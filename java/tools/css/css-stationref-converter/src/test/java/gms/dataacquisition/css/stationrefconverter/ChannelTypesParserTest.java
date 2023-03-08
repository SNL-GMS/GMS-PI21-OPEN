package gms.dataacquisition.css.stationrefconverter;

import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ChannelTypesParserTest {

  @ParameterizedTest
  @MethodSource("createChannelArguments")
  void testParseChannelTypes(String channelName, ChannelDataType channelDataType,
    ChannelBandType channelBandType, ChannelInstrumentType channelInstrumentType,
    ChannelOrientationType channelOrientationType, char orientationCode) {
    ChannelTypesParser.parseChannelTypes(channelName)
      .ifPresentOrElse(channelTypes -> assertAll(
          () -> assertEquals(channelBandType, channelTypes.getBandType()),
          () -> assertEquals(channelInstrumentType, channelTypes.getInstrumentType()),
          () -> assertEquals(channelOrientationType, channelTypes.getOrientationType()),
          () -> assertEquals(orientationCode, channelTypes.getOrientationCode()),
          () -> assertEquals(channelDataType, channelTypes.getDataType())),
        () -> fail("Datatype parsed unsuccessfully"));
  }

  static Stream<Arguments> createChannelArguments() {
    return Stream.of(
      //Test standard FDSN Seismic Channel Code parsing
      Arguments.arguments("BHZ", ChannelDataType.SEISMIC, ChannelBandType.BROADBAND,
        ChannelInstrumentType.HIGH_GAIN_SEISMOMETER, ChannelOrientationType.VERTICAL, 'Z'),
      //Test standard FDSN Non-Seismic Channel Code parsing
      Arguments.arguments("LEA", ChannelDataType.DIAGNOSTIC_SOH, ChannelBandType.LONG_PERIOD,
        ChannelInstrumentType.ELECTRONIC_TEST_POINT, ChannelOrientationType.UNKNOWN, 'A'),
      //Test IMS Seismic Channel Code parsing
      Arguments.arguments("SE", ChannelDataType.SEISMIC, ChannelBandType.SHORT_PERIOD,
        ChannelInstrumentType.UNKNOWN, ChannelOrientationType.EAST_WEST, 'E'),
      //Test IMS Wind Channel Code parsing (edge case)
      Arguments.arguments("WD", ChannelDataType.WEATHER, ChannelBandType.UNKNOWN,
        ChannelInstrumentType.WIND, ChannelOrientationType.UNKNOWN, 'D'),
      Arguments.arguments("WS", ChannelDataType.WEATHER, ChannelBandType.UNKNOWN,
        ChannelInstrumentType.WIND, ChannelOrientationType.UNKNOWN, 'S'),
      //Test IMS Temperature Channel Code parsing (edge case)
      Arguments.arguments("WT", ChannelDataType.WEATHER, ChannelBandType.UNKNOWN,
        ChannelInstrumentType.TEMPERATURE, ChannelOrientationType.UNKNOWN, 'T')
    );
  }

}