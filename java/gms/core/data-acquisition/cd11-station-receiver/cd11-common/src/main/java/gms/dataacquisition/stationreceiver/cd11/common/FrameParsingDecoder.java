package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.common.annotations.VisibleForTesting;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;


public class FrameParsingDecoder extends ByteToMessageDecoder {

  private static final Logger logger = LoggerFactory.getLogger(FrameParsingDecoder.class);

  //constants for size of header/body/trailer fields
  @VisibleForTesting
  static final int HEADER_SIZE = (Integer.BYTES * 3) + Long.BYTES + 8 + 8;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

    var startReaderIndex = in.readerIndex();
    var startWriterIndex = in.writerIndex();

    try {

      if (in.readableBytes() < HEADER_SIZE) {
        logger.debug("Readable bytes is less than the size of a CD1.1 header. Waiting for more bytes...");
        return;
      }

      var frameTypeInt = in.getInt(0);
      FrameType.fromInt(frameTypeInt);
      var trailerOffset = in.getInt(Integer.BYTES);
      checkArgument(trailerOffset >= HEADER_SIZE,
        String.format(
          "The offset of the frame trailer (%d) must be at least the size of the header (%d)",
          trailerOffset, HEADER_SIZE));

      int trailerSize = 2 * Integer.BYTES;
      if (in.readableBytes() < trailerOffset + trailerSize) {
        logger.debug("Not enough readable bytes to decode CD1.1 frame. Waiting for more bytes...");
        return;
      }
      var trailerAuthSize = in.getInt(trailerOffset + Integer.BYTES);
      checkArgument(trailerAuthSize >= 0,
        "The authentication size of the frame trailer must be greater than 0, was "
          + trailerAuthSize);
      int paddedAuthValSize = FrameUtilities
        .calculatePaddedLength(trailerAuthSize, Integer.BYTES);
      checkArgument(paddedAuthValSize >= 0,
        "The padded trailer authentication value size must be greater than 0, was "
          + paddedAuthValSize);

      int totalSize = trailerOffset + trailerSize + paddedAuthValSize + Long.BYTES;
      if (in.readableBytes() < totalSize) {
        return;
      }

      var frameBytes = new byte[totalSize];
      in.readBytes(frameBytes);
      out.add(frameBytes);
      in.discardReadBytes();
      logger.debug("Frame decoding successful");

    } catch (IllegalArgumentException e) {
      logger.warn("Frame decoding failed. Forwarding raw readable bytes.", e);

      in.setIndex(startReaderIndex, startWriterIndex);

      var rawBytes = new byte[in.readableBytes()];
      in.readBytes(rawBytes);
      out.add(rawBytes);
      in.discardReadBytes();
    } catch (NegativeArraySizeException e) {
      logger.warn("Encountered negative array size when decoding frame bytes", e.fillInStackTrace());
    }
  }
}