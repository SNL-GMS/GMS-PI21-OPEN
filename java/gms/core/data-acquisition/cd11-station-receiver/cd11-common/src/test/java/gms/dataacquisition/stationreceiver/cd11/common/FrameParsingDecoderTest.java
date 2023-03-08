package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Trailer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static gms.dataacquisition.stationreceiver.cd11.common.FrameParsingDecoder.HEADER_SIZE;
import static io.netty.buffer.Unpooled.buffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FrameParsingDecoderTest {

  @Mock
  private ChannelHandlerContext channelHandlerContext;

  private FrameParsingDecoder frameParsingDecoder;

  private List<Object> objectList;

  @BeforeEach
  void setUp() {
    frameParsingDecoder = new FrameParsingDecoder();
    objectList = new ArrayList<>();
  }

  @Test
  void testDecodeEmptyBuffer() {
    ByteBuf emptyByteBuf = new EmptyByteBuf(new PooledByteBufAllocator(true));

    var spyObjectList = spy(objectList);
    frameParsingDecoder.decode(channelHandlerContext, emptyByteBuf, spyObjectList);
    verifyNoInteractions(spyObjectList);
    assertEquals(0, objectList.size());
  }

  @ParameterizedTest
  @MethodSource("lessThanHeaderSource")
  void testDecodeLessThanHeader(ByteBuf inBuffer) {

    var spyObjectList = spy(objectList);
    frameParsingDecoder.decode(channelHandlerContext, inBuffer, objectList);
    verifyNoInteractions(spyObjectList);
    assertEquals(0, objectList.size());
  }

  public static Stream<Arguments> lessThanHeaderSource() {
    var header = Cd11Header.create(FrameType.ALERT, HEADER_SIZE, "CREATOR", "DEST", 999L, 1);
    return Stream.of(
      Arguments.arguments(buffer()),
      // Not enough header data
      Arguments.arguments(buffer().writeByte('a')),
      // No Trailer Data
      Arguments.arguments(buffer().writeBytes(header.toBytes())),
      // Not enough data in trailer
      Arguments.arguments(buffer()
        .writeBytes(header.toBytes())
        .writeInt(1)
        .writeInt(999)
        .writeBytes("LESS THAN IDEAL".getBytes()))
    );
  }

  @Test
  void testDecodeInvalidFrameType() {
    // set the frame type as in invalid option
    ByteBuf inBuffer = buffer(HEADER_SIZE, HEADER_SIZE);
    // simulate incorrect FrameType
    inBuffer.writeInt(-9999);
    // Dummy header bytes to satisfy size validation
    for (int i = Integer.BYTES; i < HEADER_SIZE; i++) {
      inBuffer.writeByte('a');
    }

    int expectedSize = inBuffer.readableBytes();
    frameParsingDecoder.decode(channelHandlerContext, inBuffer, objectList);
    assertEquals(expectedSize, ((byte[]) objectList.get(0)).length);
  }

  @Test
  void testDecodeMinimalFrame() {
    var header = Cd11Header.create(FrameType.ALERT, HEADER_SIZE, "CREATOR", "DEST", 999L, 1);
    String authValue = "TEST";
    var trailer = Cd11Trailer.from(1, authValue.length(), authValue.getBytes(), 1L);


    int minimalFrameSize = header.toBytes().length + trailer.toBytes().length;
    ByteBuf directBuffer = buffer(minimalFrameSize, minimalFrameSize);
    directBuffer.writeBytes(header.toBytes());
    directBuffer.writeBytes(trailer.toBytes());

    frameParsingDecoder.decode(channelHandlerContext, directBuffer, objectList);
    assertEquals(minimalFrameSize, ((byte[]) objectList.get(0)).length);
    assertEquals(0, directBuffer.readableBytes());
  }

  @Test
  void testDecodeFrameExcludeTrailingGarbage() {
    var header = Cd11Header.create(FrameType.ALERT, HEADER_SIZE, "CREATOR", "DEST", 999L, 1);
    var trailer = Cd11Trailer.from(1, 4, "TEST".getBytes(), 1L);


    int minimalFrameSize = header.toBytes().length + trailer.toBytes().length;
    byte[] garbageBytes = "SOME DANG OL' GARBAGE".getBytes();

    ByteBuf directBuffer = buffer(minimalFrameSize + garbageBytes.length, minimalFrameSize + garbageBytes.length);
    directBuffer.writeBytes(header.toBytes());
    directBuffer.writeBytes(trailer.toBytes());
    directBuffer.writeBytes(garbageBytes);

    frameParsingDecoder.decode(channelHandlerContext, directBuffer, objectList);
    assertEquals(minimalFrameSize, ((byte[]) objectList.get(0)).length);
    assertEquals(garbageBytes.length, directBuffer.readableBytes());
  }
}
