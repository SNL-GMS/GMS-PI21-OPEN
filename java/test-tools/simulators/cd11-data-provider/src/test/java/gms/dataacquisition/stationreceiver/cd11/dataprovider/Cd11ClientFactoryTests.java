package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class Cd11ClientFactoryTests {

  public static final String FRAME_CREATOR = "creator";
  public static final String FRAME_DESTINATION = "dest";

  public static final String TEST_ADDRESS = "test";
  public static final int TEST_PORT = 0;

  public static final String TEST_STATION = "TEST";

  @Mock
  Socket clientSocket;

  @Mock
  InputStream inputStream;

  @Mock
  OutputStream outputStream;

  Cd11FrameFactory frameFactory;

  @Spy
  Cd11ClientFactory factorySpy = Cd11ClientFactory.create(FRAME_CREATOR, FRAME_DESTINATION);

  @BeforeEach
  void setUp() {
    frameFactory = Cd11FrameFactory.createUnauthenticated(FRAME_CREATOR, FRAME_DESTINATION);
  }

  @Test
  void testRequestConnection() throws IOException {
    willReturn(clientSocket)
      .given(factorySpy)
      .getClientSocket(TEST_ADDRESS, TEST_PORT);

    //secondary exchange address info must be set to zero to adhere to serialization protocol of CD1.1
    Cd11ConnectionExchange expectedResponse = Cd11ConnectionExchange.builder()
      .setStationOrResponderName("TEST")
      .setStationOrResponderType("IDC")
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 0)
      .setServiceType("TCP")
      .setIpAddress(127001)
      .setPort(TEST_PORT)
      .setSecondIpAddress(0)
      .setSecondPort(0)
      .build();

    Cd11Frame expectedFrame = frameFactory.wrapResponse(expectedResponse);

    given(clientSocket.getOutputStream()).willReturn(outputStream);
    given(clientSocket.getInputStream())
      .willReturn(new ByteArrayInputStream(expectedFrame.toBytes()));

    Cd11ConnectionExchange actualResponse = assertDoesNotThrow(
      () -> factorySpy.tryRequestConnection(TEST_ADDRESS, TEST_PORT, TEST_STATION));
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void testCreateClientManager() {
    int expectedIpAddress = 127001;
    String expectedIpString = "0.1.240.25";

    Cd11ConnectionExchange connectionResponse = Cd11ConnectionExchange.builder()
      .setStationOrResponderName("TEST")
      .setStationOrResponderType("IDC")
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 0)
      .setServiceType("TCP")
      .setIpAddress(expectedIpAddress)
      .setPort(TEST_PORT)
      .build();

    var actualClient = assertDoesNotThrow(
      () -> factorySpy.createCd11Client(TEST_STATION, connectionResponse, Flux.empty()));
    assertNotNull(actualClient);

    var actualAddress = actualClient.getDelegate().configuration().remoteAddress().get();
    assertTrue(actualAddress instanceof InetSocketAddress);

    var actualInet = (InetSocketAddress) actualAddress;
    assertEquals(expectedIpString, actualInet.getHostString());
    assertEquals(TEST_PORT, actualInet.getPort());
  }

  @Test
  void testFailedConnectionManagerConnection() throws IOException {
    IOException expectedException = new IOException("Failed to connect");
    willThrow(expectedException)
      .given(factorySpy).getClientSocket(TEST_ADDRESS, TEST_PORT);

    IOException actualException = assertThrows(IOException.class,
      () -> factorySpy.tryRequestConnection(TEST_ADDRESS, TEST_PORT, TEST_STATION));
    assertEquals(expectedException, actualException);
  }

  @Test
  void testFailedWrite() throws IOException {
    willReturn(clientSocket)
      .given(factorySpy)
      .getClientSocket(TEST_ADDRESS, TEST_PORT);
    given(clientSocket.getOutputStream()).willReturn(outputStream);

    IOException expectedException = new IOException("Failed to write");
    willThrow(expectedException).given(outputStream).write(any());

    IOException actualException = assertThrows(IOException.class,
      () -> factorySpy.tryRequestConnection(TEST_ADDRESS, TEST_PORT, TEST_STATION));
    assertEquals(expectedException, actualException);
  }

  @Test
  void testFailedRead() throws IOException {
    willReturn(clientSocket)
      .given(factorySpy)
      .getClientSocket(TEST_ADDRESS, TEST_PORT);
    given(clientSocket.getOutputStream()).willReturn(outputStream);
    given(clientSocket.getInputStream()).willReturn(inputStream);

    IOException expectedException = new IOException("Failed to read");
    given(inputStream.read(any())).willThrow(expectedException);

    IOException actualException = assertThrows(IOException.class,
      () -> factorySpy.tryRequestConnection(TEST_ADDRESS, TEST_PORT, TEST_STATION));
    assertEquals(expectedException, actualException);
  }

  @Test
  void testEndOfStreamFails() throws IOException {
    willReturn(clientSocket)
      .given(factorySpy)
      .getClientSocket(TEST_ADDRESS, TEST_PORT);
    given(clientSocket.getOutputStream()).willReturn(outputStream);
    given(clientSocket.getInputStream()).willReturn(inputStream);

    given(inputStream.read(any())).willReturn(-1);

    assertThrows(IOException.class,
      () -> factorySpy.tryRequestConnection(TEST_ADDRESS, TEST_PORT, TEST_STATION));
  }
}