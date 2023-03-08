package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframeHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandRequest;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponse;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import gms.dataacquisition.stationreceiver.cd11.common.frames.CustomReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.calculatePaddedLength;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.jdToInstant;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.readBytesAsString;

public class Cd11PayloadReader {

  private static final Logger logger = LoggerFactory.getLogger(Cd11PayloadReader.class);
  private static final String NON_NEGATIVE_ERROR = " value must be non-negative";

  private Cd11PayloadReader() {
    //Private constructor declared to prevent instantiation of factory class
  }

  public static Cd11Payload tryReadPayload(FrameType frameType, ByteBuffer payloadBytes) {
    // Construct the appropriate CD 1.1 frame.
    switch (frameType) {
      case ACKNACK:
        return tryReadAcknack(payloadBytes);
      case ALERT:
        return tryReadAlert(payloadBytes);
      case COMMAND_REQUEST:
        return tryReadCommandRequest(payloadBytes);
      case COMMAND_RESPONSE:
        return tryReadCommandResponse(payloadBytes);
      case CONNECTION_REQUEST:
      case CONNECTION_RESPONSE:
        return tryReadConnectionExchange(payloadBytes);
      case DATA:
      case CD_ONE_ENCAPSULATION:
        return tryReadData(payloadBytes);
      case OPTION_REQUEST:
      case OPTION_RESPONSE:
        return tryReadOptionExchange(payloadBytes);
      case CUSTOM_RESET_FRAME:
        return CustomReset.create(payloadBytes.array());
      default:
        throw new IllegalArgumentException(
          String.format("Frame type does not exist.%s", frameType));
    }
  }

  public static Cd11Acknack tryReadAcknack(ByteBuffer payloadBytes) {
    //throws ParseCd11FromByteBufferException, IllegalArgumentException
    try {
      var cd11AckNackBuilder = Cd11Acknack.builder();

      //Size of frameset Acked is 20 bytes per the CD11 specification
      cd11AckNackBuilder.setFrameSetAcked(readBytesAsString(payloadBytes, 20))
        .setLowestSeqNum(payloadBytes.getLong())
        .setHighestSeqNum(payloadBytes.getLong());

      var gapCount = payloadBytes.getInt();
      checkArgument(gapCount >= 0, "gapCount" + NON_NEGATIVE_ERROR);
      var gapsArray = new long[gapCount * 2];
      for (var i = 0; i < gapCount * 2; i++) {
        gapsArray[i] = payloadBytes.getLong();
      }
      return cd11AckNackBuilder.setGapCount(gapCount)
        .setGapRanges(gapsArray)
        .build();
    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(Cd11FrameReader.PARSING_ERROR_MESSAGE + "Cd11Acknack", e);
    }
  }

  public static Cd11Alert tryReadAlert(ByteBuffer payloadBytes) {
    // throws ParseCd11FromByteBufferException, IllegalArgumentException
    try {
      var sizeInt = payloadBytes.getInt();
      var messageString = readBytesAsString(payloadBytes, calculatePaddedLength(sizeInt, Integer.BYTES));
      return Cd11Alert.create(sizeInt, messageString);
    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(Cd11FrameReader.PARSING_ERROR_MESSAGE + "Cd11Alert", e);
    }
  }

  public static Cd11CommandRequest tryReadCommandRequest(ByteBuffer payloadBytes) {
    //throws ParseCd11FromByteBufferException, IllegalArgument Exception
    try {
      var cd11CommandRequestBuilder = Cd11CommandRequest.builder();

      cd11CommandRequestBuilder
        .setStationName(readBytesAsString(payloadBytes, 8))
        .setSite(readBytesAsString(payloadBytes, 5))
        .setChannel(readBytesAsString(payloadBytes, 3))
        .setLocName(readBytesAsString(payloadBytes, 2));

      payloadBytes.position(payloadBytes.position() + 2); // Skip two null bytes.
      cd11CommandRequestBuilder.setTimestamp(jdToInstant(readBytesAsString(payloadBytes, 20)));
      var commandMessageSize = payloadBytes.getInt();
      return cd11CommandRequestBuilder.setCommandMessage(readBytesAsString(payloadBytes, commandMessageSize))
        .build();
    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(
        Cd11FrameReader.PARSING_ERROR_MESSAGE + "Cd11CommandRequest", e);
    }
  }

  public static Cd11CommandResponse tryReadCommandResponse(ByteBuffer payloadBytes) {
    //throws ParseCd11FromByteBufferException, IllegalArgument Exception
    try {
      var cd11CommandResponseBuilder = Cd11CommandResponse.builder();

      cd11CommandResponseBuilder
        .setResponderStation(readBytesAsString(payloadBytes, 8))
        .setSite(readBytesAsString(payloadBytes, 5))
        .setChannel(readBytesAsString(payloadBytes, 3))
        .setLocName(readBytesAsString(payloadBytes, 2));

      payloadBytes.position(payloadBytes.position() + 2); // Skip two null bytes.
      cd11CommandResponseBuilder.setTimestamp(jdToInstant(readBytesAsString(payloadBytes, 20)));
      var commandMessageSize = payloadBytes.getInt();
      cd11CommandResponseBuilder.setCommandRequestMessage(readBytesAsString(payloadBytes, commandMessageSize));
      var responseMessageSize = payloadBytes.getInt();
      return cd11CommandResponseBuilder.setResponseMessage(readBytesAsString(payloadBytes, responseMessageSize))
        .build();
    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(
        Cd11FrameReader.PARSING_ERROR_MESSAGE + "Cd11CommandRequest", e);
    }
  }

  public static Cd11ConnectionExchange tryReadConnectionExchange(ByteBuffer payloadBytes) {
    //throws ParseCd11FromByteBufferException, IllegalArgumentException
    try {
      var cd11ConnectionExchangeBuilder = Cd11ConnectionExchange.builder();

      return cd11ConnectionExchangeBuilder
        .setMajorVersion(payloadBytes.getShort())
        .setMinorVersion(payloadBytes.getShort())
        .setStationOrResponderName(readBytesAsString(payloadBytes, 8))
        .setStationOrResponderType(readBytesAsString(payloadBytes, 4))
        .setServiceType(readBytesAsString(payloadBytes, 4))
        .setIpAddress(payloadBytes.getInt())
        .setPort(payloadBytes.getChar()) // Convert "unsigned short" to a Java "int".
        .setSecondIpAddress(payloadBytes.getInt())
        .setSecondPort(payloadBytes.getChar())
        .build(); // Convert "unsigned short" to a Java "int".

    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(Cd11FrameReader.PARSING_ERROR_MESSAGE +
        "Cd11ConnectionRequest or Response", e);
    }
  }

  public static Cd11Data tryReadData(ByteBuffer payloadBytes) {
    //throws ParseCd11FromByteBufferException, IllegalArgumentException, DateTimeParseException

    Cd11ChannelSubframeHeader subframeHeader = tryReadChannelSubframeHeader(payloadBytes);
    //there are 10 bytes for each channel subframe in the channel string
    int numOfSubFrames = subframeHeader.channelString.length() / 10;
    List<Cd11ChannelSubframe> tempSubFrames = new ArrayList<>();
    for (var i = 0; i < numOfSubFrames; i++) {
      tempSubFrames.add(new Cd11ChannelSubframe(payloadBytes));
    }

    if (payloadBytes.remaining() > 0) {
      logger.warn("Not all bytes of Data Frame body parsed, {} remaining", payloadBytes.remaining());
    }

    return Cd11Data.builder()
      .setChanSubframeHeader(subframeHeader)
      .setChannelSubframes(tempSubFrames)
      .build();
  }

  private static Cd11ChannelSubframeHeader tryReadChannelSubframeHeader(ByteBuffer body) {
    //throws ParseCd11FromByteBufferException, IllegalArgumentException
    Cd11ChannelSubframeHeader cd11ChannelSubframeHeader;
    try {
      cd11ChannelSubframeHeader = Cd11ChannelSubframeHeader.read(body);
    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(
        Cd11FrameReader.PARSING_ERROR_MESSAGE + "Cd11ChannelSubframeHeader", e);
    }
    return cd11ChannelSubframeHeader;
  }

  public static Cd11OptionExchange tryReadOptionExchange(ByteBuffer payloadBytes) {
    // throws IOException, IllegalArgumentException, NullPointerException
    try {
      var cd11OptionsExchangeBuilder = Cd11OptionExchange.builder();

      payloadBytes.getInt();
      cd11OptionsExchangeBuilder.setOptionType(payloadBytes.getInt());
      var optionSize = payloadBytes.getInt();
      var optionValueString = readBytesAsString(payloadBytes, calculatePaddedLength(optionSize, 4));
      return cd11OptionsExchangeBuilder
        .setOptionValue(optionValueString)
        .build();
    } catch (BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(
        Cd11FrameReader.PARSING_ERROR_MESSAGE + "Cd11OptionRequestorResponse", e);
    }
  }
}
