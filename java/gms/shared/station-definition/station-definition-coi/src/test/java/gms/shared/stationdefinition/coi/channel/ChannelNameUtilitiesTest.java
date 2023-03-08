package gms.shared.stationdefinition.coi.channel;

import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ChannelNameUtilitiesTest {

  @Test
  void testCreateName() {
    String expected = "STA.GROUP.BHZ/" + ChannelNameUtilities.NameHash.builder(CHANNEL).build().getHash();
    String actual = ChannelNameUtilities.createName(CHANNEL);
    assertEquals(expected, actual);
  }

  @Test
  void testCreateShortNameChannel() {
    String expected = "STA.GROUP.BHZ";
    String actual = ChannelNameUtilities.createShortName(CHANNEL);
    assertEquals(expected, actual);
  }

  @Test
  void testCreateShortNameStrings() {
    String expected = "STA.CHAN.BHZ";
    String actual = ChannelNameUtilities.createShortName("STA", "CHAN", "BHZ");
    assertEquals(expected, actual);
  }

  @Test
  void testGetFdsnChannelName() {
    String expected = "BHZ";
    String actual = ChannelNameUtilities.getFdsnChannelName(CHANNEL);
    assertEquals(expected, actual);
  }

  @Test
  void testCreateAttributesForChannelName() {
    Map<ChannelProcessingMetadataType, Object> metadata = Map.of(ChannelProcessingMetadataType.FILTER_TYPE, "FIR",
      ChannelProcessingMetadataType.FILTER_PASS_BAND_TYPE, "TestPassBandType",
      ChannelProcessingMetadataType.FILTER_LOW_FREQUENCY_HZ, 23.1234,
      ChannelProcessingMetadataType.FILTER_HIGH_FREQUENCY_HZ, 45.3456,
      ChannelProcessingMetadataType.BEAM_COHERENT, "TEST_BEAM_PROPERTIES",
      ChannelProcessingMetadataType.STEERING_AZIMUTH, 0.12345,
      ChannelProcessingMetadataType.STEERING_SLOWNESS, 12.34567);

    String expected = "/filter,fir,testpassbandtype_23.12hz_45.35hz" +
      "/steer,az_0.123deg,slow_12.346s_per_deg";

    String actual = ChannelNameUtilities.createAttributesForChannelName(metadata);
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("getCreateFilterAttributeForChannelNameArguments")
  void testCreateFilterAttributeForChannelName(Map<ChannelProcessingMetadataType, Object> metadata,
    String expected) {
    String actual = ChannelNameUtilities.createFilterAttributeForChannelName(metadata);
    assertEquals(expected, actual);
  }

  static Stream<Arguments> getCreateFilterAttributeForChannelNameArguments() {
    return Stream.of(arguments(Map.of(), ""),
      arguments(Map.of(ChannelProcessingMetadataType.FILTER_TYPE, "FIR",
          ChannelProcessingMetadataType.FILTER_PASS_BAND_TYPE, "TestPassBandType",
          ChannelProcessingMetadataType.FILTER_LOW_FREQUENCY_HZ, 23.1234,
          ChannelProcessingMetadataType.FILTER_HIGH_FREQUENCY_HZ, 45.3456),
        "/filter,fir,testpassbandtype_23.12hz_45.35hz"));
  }

  @ParameterizedTest
  @MethodSource("getCreateBeamAttributeForChannelNameArguments")
  void testCreateBeamAttributeForChannelName(Map<ChannelProcessingMetadataType, Object> metadata,
    String expected) {
    String actual = ChannelNameUtilities.createBeamAttributeForChannelName(metadata);
    assertEquals(expected, actual);
  }

  static Stream<Arguments> getCreateBeamAttributeForChannelNameArguments() {
    return Stream.of(arguments(Map.of(), ""),
      arguments(Map.of(ChannelProcessingMetadataType.BEAM_COHERENT, "TEST_BEAM_PROPERTIES",
          ChannelProcessingMetadataType.BEAM_TYPE, BeamType.EVENT),
        "/beam,event,incoherent"),
      arguments(Map.of(ChannelProcessingMetadataType.BEAM_COHERENT, ChannelType.I,
          ChannelProcessingMetadataType.BEAM_TYPE, BeamType.FK),
        "/beam,fk,incoherent"),
      arguments(Map.of(ChannelProcessingMetadataType.BEAM_COHERENT, ChannelType.N,
          ChannelProcessingMetadataType.BEAM_TYPE, BeamType.DETECTION),
        "/beam,detection,incoherent"),
      arguments(Map.of(ChannelProcessingMetadataType.BEAM_COHERENT, ChannelType.B,
          ChannelProcessingMetadataType.BEAM_TYPE, BeamType.EVENT),
        "/beam,event,coherent"));
  }

  @ParameterizedTest
  @MethodSource("getCreateSteerAttributeForChannelNameArguments")
  void testCreateSteerAttributeForChannelName(Map<ChannelProcessingMetadataType, Object> metadata,
    String expected) {
    String actual = ChannelNameUtilities.createSteerAttributeForChannelName(metadata);
    assertEquals(expected, actual);
  }

  static Stream<Arguments> getCreateSteerAttributeForChannelNameArguments() {
    return Stream.of(arguments(Map.of(), ""),
      arguments(Map.of(ChannelProcessingMetadataType.STEERING_AZIMUTH, 0.12345,
          ChannelProcessingMetadataType.STEERING_SLOWNESS, 12.34567),
        "/steer,az_0.123deg,slow_12.346s_per_deg"));
  }

}