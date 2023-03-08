package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Trailer;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.PartialFrame;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class Cd11FrameReader {

  private Cd11FrameReader() {
    //Hiding implicit public constructor
  }

  static final String PARSING_ERROR_MESSAGE = "Fewer bytes than expected when reading from Byte Buffer for ";
  //constants for size of header/body/trailer fields
  private static final int HEADER_SIZE = (Integer.BYTES * 3) + Long.BYTES + 8 + 8;

  /**
   * Partially read enough information from the Frame bytes to allow further processing
   *
   * @param buf Bytes to read
   *
   * @return A PartialFrame populated with some initial frame metadata
   *
   * @throws ParseCd11FromByteBufferException if there were any overflow/underflow exceptions when reading from the byte
   * buffer
   */
  public static Cd11OrMalformedFrame readFrame(ByteBuffer buf) {
    Cd11Header header = null;
    Cd11Payload body = null;
    Cd11Trailer trailer = null;
    try {
      header = tryReadHeader(buf);
      body = tryReadBody(buf, header);
      trailer = tryReadTrailer(buf);

      return Cd11OrMalformedFrame.ofCd11(
        Cd11Frame.builder()
          .setHeader(header)
          .setPayload(body)
          .setTrailer(trailer)
          .build());
    }
    catch (Exception e) {
      Optional<Cd11Header> maybeHeader = Optional.ofNullable(header);
      Optional<Cd11Payload> maybeBody = Optional.ofNullable(body);
      Optional<Cd11Trailer> maybeTrailer = Optional.ofNullable(trailer);

      var partialBuilder = PartialFrame.builder();
      var malformedBuilder = MalformedFrame.builder();
      maybeHeader.ifPresent(partialBuilder::setHeader);
      maybeHeader.map(Cd11Header::getFrameCreator).ifPresent(malformedBuilder::setStation);
      maybeBody.ifPresent(partialBuilder::setBody);
      maybeTrailer.ifPresent(partialBuilder::setTrailer);

      return Cd11OrMalformedFrame.ofMalformed(malformedBuilder
        .setPartialFrame(partialBuilder.build())
        .setCause(e)
        .setReadPosition(buf.position())
        .setBytes(buf.rewind().array())
        .build());
    }
  }

  static Cd11Header tryReadHeader(ByteBuffer buf) {
    //throws ParseCd11FromByteBufferException, IllegalArgumentException
    Cd11Header cd11Header;
    try {
      cd11Header = Cd11Header.read(buf);
    }
    catch (IllegalArgumentException | BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(PARSING_ERROR_MESSAGE + "Cd11 Header", e);
    }
    return cd11Header;
  }

  static Cd11Payload tryReadBody(ByteBuffer buf, Cd11Header frameHeader) {
    //throws ParseCd11FromByteBufferException
    try {
      byte[] bodyBytes;
      int requiredBodyBytes = frameHeader.getTrailerOffset() - HEADER_SIZE;
      bodyBytes = FrameUtilities.readBytes(buf, requiredBodyBytes);
      return Cd11PayloadReader.tryReadPayload(frameHeader.getFrameType(), ByteBuffer.wrap(bodyBytes));
    }
    catch (IllegalArgumentException | BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(PARSING_ERROR_MESSAGE + "Cd11 Body Bytes", e);
    }
  }

  static Cd11Trailer tryReadTrailer(ByteBuffer buf) {
    //throws ParseCd11FromByteBufferException, IllegalArgumentException

    Cd11Trailer frameTrailer;
    try {
      frameTrailer = Cd11Trailer.read(buf);
    }
    catch (IllegalArgumentException | BufferOverflowException | BufferUnderflowException | NegativeArraySizeException e) {
      throw new ParseCd11FromByteBufferException(PARSING_ERROR_MESSAGE + "Cd11 Trailer", e);
    }
    return frameTrailer;
  }
}
