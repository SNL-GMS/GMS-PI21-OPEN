package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.common.primitives.Longs;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.Cd11DataFormat;
import gms.dataacquisition.stationreceiver.cd11.common.enums.CompressionFormat;
import gms.dataacquisition.stationreceiver.cd11.common.enums.SensorType;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

public class Cd11PayloadFixtures {

  static final int TWO_CHANNELS = 2;
  static final int FRAME_TIME_LENGTH = 10000;
  static final Instant NOMINAL_TIME = Instant.parse("2017-12-06T17:15:00Z");
  static final int CHANNEL_STRING_COUNT2 = 20;
  static final String CHANNEL_STRING = "STA12SHZ01";
  static final String CHANNEL_STRING2 = "STA12SHZ02";
  static final int DATA_SUBFRAME_SIZE = 100;
  static final int CHANNEL_LENGTH = 96;
  static final int AUTHENTICATION_OFFSET = DATA_SUBFRAME_SIZE - 16;
  static final boolean CHANNEL_DESCRIPTION_AUTHENTICATION = false;
  static final CompressionFormat CHANNEL_DESCRIPTION_TRANSFORMATION = CompressionFormat.CANADIAN_AFTER_SIGNATURE;
  static final SensorType CHANNEL_DESCRIPTION_SENSOR_TYPE = SensorType.HYDROACOUSTIC;
  static final boolean CHANNEL_DESCRIPTION_OPTION_FLAG = false;
  static final String CHANNEL_DESCRIPTION_SITE_NAME = "STA12";
  static final String CHANNEL_DESCRIPTION_CHANNEL_NAME = "SHZ";
  static final String CHANNEL_DESCRIPTION_LOCATION = "01";
  static final String CHANNEL_DESCRIPTION_LOCATION2 = "02";
  static final Cd11DataFormat CHANNEL_DESCRIPTION_DATA_FORMAT = Cd11DataFormat.S4;
  static final int CHANNEL_DESCRIPTION_CALIB_FACTOR = 0;
  static final int CHANNEL_DESCRIPTION_CALIB_PER = 0;
  static final Instant TIME_STAMP = Instant.parse("2017-12-01T17:15:00.123Z");
  static final int SUBFRAME_TIME_LENGTH = 10000;
  static final int SAMPLES = 8;
  static final int CHANNEL_STATUS_SIZE = 4;
  static final byte[] CHANNEL_STATUS = new byte[4];
  static final int DATA_SIZE = 8;
  static final byte[] CHANNEL_DATA = new byte[8];
  static final int SUBFRAME_COUNT = 0;
  static final int AUTH_KEY = 123;
  static final int AUTH_SIZE = 8;
  static final byte[] AUTH_VALUE = Longs.toByteArray(1512076158000L);

  public static Cd11Data cd11Data() {
    Cd11ChannelSubframeHeader expectedHeader = new Cd11ChannelSubframeHeader(
      TWO_CHANNELS, FRAME_TIME_LENGTH, NOMINAL_TIME, CHANNEL_STRING_COUNT2,
      CHANNEL_STRING + CHANNEL_STRING2
    );

    Cd11ChannelSubframe subframe1 = new Cd11ChannelSubframe(
      CHANNEL_LENGTH, AUTHENTICATION_OFFSET, CHANNEL_DESCRIPTION_AUTHENTICATION,
      CHANNEL_DESCRIPTION_TRANSFORMATION,
      CHANNEL_DESCRIPTION_SENSOR_TYPE, CHANNEL_DESCRIPTION_OPTION_FLAG,
      CHANNEL_DESCRIPTION_SITE_NAME, CHANNEL_DESCRIPTION_CHANNEL_NAME,
      CHANNEL_DESCRIPTION_LOCATION, CHANNEL_DESCRIPTION_DATA_FORMAT,
      CHANNEL_DESCRIPTION_CALIB_FACTOR, CHANNEL_DESCRIPTION_CALIB_PER,
      TIME_STAMP, SUBFRAME_TIME_LENGTH, SAMPLES, CHANNEL_STATUS_SIZE, CHANNEL_STATUS, DATA_SIZE,
      CHANNEL_DATA, SUBFRAME_COUNT, AUTH_KEY, AUTH_SIZE, AUTH_VALUE);

    Cd11ChannelSubframe subframe2 = new Cd11ChannelSubframe(
      CHANNEL_LENGTH, AUTHENTICATION_OFFSET, CHANNEL_DESCRIPTION_AUTHENTICATION,
      CHANNEL_DESCRIPTION_TRANSFORMATION,
      CHANNEL_DESCRIPTION_SENSOR_TYPE, CHANNEL_DESCRIPTION_OPTION_FLAG,
      CHANNEL_DESCRIPTION_SITE_NAME, CHANNEL_DESCRIPTION_CHANNEL_NAME,
      CHANNEL_DESCRIPTION_LOCATION2, CHANNEL_DESCRIPTION_DATA_FORMAT,
      CHANNEL_DESCRIPTION_CALIB_FACTOR, CHANNEL_DESCRIPTION_CALIB_PER,
      TIME_STAMP, SUBFRAME_TIME_LENGTH, SAMPLES, CHANNEL_STATUS_SIZE, CHANNEL_STATUS, DATA_SIZE,
      CHANNEL_DATA, SUBFRAME_COUNT, AUTH_KEY, AUTH_SIZE, AUTH_VALUE);

    return Cd11Data.builder()
      .setChanSubframeHeader(expectedHeader)
      .setChannelSubframes(List.of(subframe1, subframe2))
      .build();
  }

  static ByteBuffer cd11DataBytes() {
    ByteBuffer dataFrameBuff = ByteBuffer.allocate(52 + DATA_SUBFRAME_SIZE * 2);

    for (int i = 0; i < CHANNEL_DATA.length; i++) {
      CHANNEL_DATA[i] = (byte) i;
    }

    // Data frame header
    dataFrameBuff.putInt(TWO_CHANNELS);
    dataFrameBuff.putInt(FRAME_TIME_LENGTH);
    dataFrameBuff.put(FrameUtilities.instantToJd(NOMINAL_TIME).getBytes());
    dataFrameBuff.putInt(CHANNEL_STRING_COUNT2);
    dataFrameBuff.put(CHANNEL_STRING.getBytes());
    dataFrameBuff.put(CHANNEL_STRING2.getBytes());

    // Channel Subframe 1
    dataFrameBuff.putInt(CHANNEL_LENGTH);
    dataFrameBuff.putInt(AUTHENTICATION_OFFSET);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_AUTHENTICATION));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_TRANSFORMATION.code);
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SENSOR_TYPE.code);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_OPTION_FLAG));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_LOCATION.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_DATA_FORMAT.toBytes());
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    dataFrameBuff.put(FrameUtilities.instantToJd(TIME_STAMP).getBytes());
    dataFrameBuff.putInt(SUBFRAME_TIME_LENGTH);
    dataFrameBuff.putInt(SAMPLES);
    dataFrameBuff.putInt(CHANNEL_STATUS_SIZE);
    dataFrameBuff.put(CHANNEL_STATUS);
    dataFrameBuff.putInt(DATA_SIZE);
    dataFrameBuff.put(CHANNEL_DATA);
    dataFrameBuff.putInt(SUBFRAME_COUNT);
    dataFrameBuff.putInt(AUTH_KEY);
    dataFrameBuff.putInt(AUTH_SIZE);
    dataFrameBuff.put(AUTH_VALUE);

    // Channel Subframe 2
    dataFrameBuff.putInt(CHANNEL_LENGTH);
    dataFrameBuff.putInt(AUTHENTICATION_OFFSET);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_AUTHENTICATION));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_TRANSFORMATION.code);
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SENSOR_TYPE.code);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_OPTION_FLAG));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_LOCATION2.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_DATA_FORMAT.toBytes());
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    dataFrameBuff.put(FrameUtilities.instantToJd(TIME_STAMP).getBytes());
    dataFrameBuff.putInt(SUBFRAME_TIME_LENGTH);
    dataFrameBuff.putInt(SAMPLES);
    dataFrameBuff.putInt(CHANNEL_STATUS_SIZE);
    dataFrameBuff.put(CHANNEL_STATUS);
    dataFrameBuff.putInt(DATA_SIZE);
    dataFrameBuff.put(CHANNEL_DATA);
    dataFrameBuff.putInt(SUBFRAME_COUNT);
    dataFrameBuff.putInt(AUTH_KEY);
    dataFrameBuff.putInt(AUTH_SIZE);
    dataFrameBuff.put(AUTH_VALUE);

    return dataFrameBuff.rewind();
  }

  private static byte toByte(boolean b) {
    return b ? (byte) 1 : (byte) 0;
  }
}
