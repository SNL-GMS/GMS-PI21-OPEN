package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Cd11DataTest {

  @Test
  void testDataRoundTripBytes() {
    ByteBuffer expectedBytes = Cd11PayloadFixtures.cd11DataBytes();
    Cd11Data actualData = Cd11PayloadReader.tryReadData(expectedBytes);

    // Test header
    Cd11ChannelSubframeHeader actualHeader = actualData.getChanSubframeHeader();
    assertEquals(Cd11PayloadFixtures.TWO_CHANNELS, actualHeader.numOfChannels);
    assertEquals(Cd11PayloadFixtures.FRAME_TIME_LENGTH, actualHeader.frameTimeLength);
    assertEquals(Cd11PayloadFixtures.NOMINAL_TIME, actualHeader.nominalTime);
    assertEquals(Cd11PayloadFixtures.CHANNEL_STRING_COUNT2, actualHeader.channelStringCount);
    assertEquals(Cd11PayloadFixtures.CHANNEL_STRING + Cd11PayloadFixtures.CHANNEL_STRING2, actualHeader.channelString);

    // Test subframes
    assertEquals(2, actualData.getChannelSubframes().size());
    Cd11ChannelSubframe firstActualSubframe = actualData.getChannelSubframes().get(0);
    assertSubframeData(firstActualSubframe, Cd11PayloadFixtures.CHANNEL_DESCRIPTION_LOCATION);


    Cd11ChannelSubframe secondActualSubframe = actualData.getChannelSubframes().get(1);
    assertSubframeData(secondActualSubframe, Cd11PayloadFixtures.CHANNEL_DESCRIPTION_LOCATION2);

    //end of trip byte comparison
    assertArrayEquals(expectedBytes.array(), actualData.toBytes());
  }

  private void assertSubframeData(Cd11ChannelSubframe subframe, String expectedLocationName) {
    assertEquals(Cd11PayloadFixtures.CHANNEL_LENGTH, subframe.channelLength);
    assertEquals(Cd11PayloadFixtures.AUTHENTICATION_OFFSET, subframe.authOffset);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_AUTHENTICATION, subframe.authenticationOn);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_TRANSFORMATION, subframe.compressionFormat);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_SENSOR_TYPE, subframe.sensorType);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_OPTION_FLAG, subframe.isCalib);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_SITE_NAME, subframe.siteName);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_CHANNEL_NAME, subframe.channelName);
    assertEquals(expectedLocationName, subframe.locationName);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_DATA_FORMAT, subframe.cd11DataFormat);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_CALIB_FACTOR, subframe.calibrationFactor,
      0.00000001);
    assertEquals(Cd11PayloadFixtures.CHANNEL_DESCRIPTION_CALIB_PER, subframe.calibrationPeriod, 0.00000001);
    assertEquals(Cd11PayloadFixtures.TIME_STAMP, subframe.timeStamp);
    assertEquals(Cd11PayloadFixtures.SUBFRAME_TIME_LENGTH, subframe.subframeTimeLength);
    assertEquals(Cd11PayloadFixtures.SAMPLES, subframe.samples);
    assertEquals(Cd11PayloadFixtures.CHANNEL_STATUS_SIZE, subframe.channelStatusSize);
    assertArrayEquals(Cd11PayloadFixtures.CHANNEL_STATUS, subframe.channelStatusData);
    assertEquals(Cd11PayloadFixtures.DATA_SIZE, subframe.dataSize);
    assertArrayEquals(Cd11PayloadFixtures.CHANNEL_DATA, subframe.channelData);
    assertEquals(Cd11PayloadFixtures.SUBFRAME_COUNT, subframe.subframeCount);
    assertEquals(Cd11PayloadFixtures.AUTH_KEY, subframe.authKeyIdentifier);
    assertEquals(Cd11PayloadFixtures.AUTH_SIZE, subframe.authSize);
    assertArrayEquals(Cd11PayloadFixtures.AUTH_VALUE, subframe.authValue);
  }

  @Test
  void testDataRoundTrip() {

    Cd11Data expectedData = Cd11PayloadFixtures.cd11Data();

    Cd11Data actualData = Cd11PayloadReader.tryReadData(ByteBuffer.wrap(expectedData.toBytes()));
    assertEquals(expectedData, actualData);
  }

  @Test
  void testPayloadRoundTrip() {
    Cd11Data expectedData = Cd11PayloadFixtures.cd11Data();

    Cd11Payload actualPayload = Cd11PayloadReader
      .tryReadPayload(FrameType.DATA, ByteBuffer.wrap(expectedData.toBytes()));
    assertEquals(expectedData, actualPayload);
  }

}
