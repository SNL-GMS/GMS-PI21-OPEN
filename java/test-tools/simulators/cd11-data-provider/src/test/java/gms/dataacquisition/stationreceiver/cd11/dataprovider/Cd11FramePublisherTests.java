package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Cd11FramePublisherTests {

  public static final String TEST_ADDRESS = "test";
  public static final int TEST_PORT = 0;
  public static final int maxAttempts = 10;

  @Mock
  private Cd11ClientFactory clientFactory;

  @Mock
  private Cd11Client cd11Client;

  Cd11FramePublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = Cd11FramePublisher.create(TEST_ADDRESS, TEST_PORT,
      clientFactory, RetryConfig.create(1, 2, ChronoUnit.NANOS, maxAttempts));
  }

  @Test
  void testPublishMultiStation() throws IOException {
    Cd11ConnectionExchange expectedResponse = Cd11ConnectionExchange.builder()
      .setStationOrResponderName("TEST")
      .setStationOrResponderType("IDC")
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 0)
      .setServiceType("TCP")
      .setIpAddress(127001)
      .setPort(TEST_PORT)
      .build();

    List<RawStationDataFrame> rsdfs = List.of(
      buildDummyRsdf("TEST1"), buildDummyRsdf("TEST2"),
      buildDummyRsdf("TEST1"), buildDummyRsdf("TEST2"));
    Flux<RawStationDataFrame> rsdfFlux = Flux.fromIterable(rsdfs);

    willReturn(expectedResponse).given(clientFactory)
      .tryRequestConnection(TEST_ADDRESS, TEST_PORT, "TEST1");
    willReturn(expectedResponse).given(clientFactory)
      .tryRequestConnection(TEST_ADDRESS, TEST_PORT, "TEST2");

    given(clientFactory.createCd11Client(anyString(), any(), any())).willReturn(cd11Client);
    given(cd11Client.connect()).willReturn(Mono.empty());

    StepVerifier.create(publisher.publish(() -> rsdfFlux))
      .verifyComplete();

    verify(clientFactory).createCd11Client(eq("TEST1"), any(), any());
    verify(clientFactory).createCd11Client(eq("TEST2"), any(), any());
    verify(cd11Client, times(2)).connect();
  }

  @Test
  void testPublishConnectionFailureDoesNotThrow() throws IOException {
    String failStation = "FAIL";
    String passStation = "PASS";
    List<RawStationDataFrame> rsdfs = List
      .of(buildDummyRsdf(failStation), buildDummyRsdf(passStation));
    Flux<RawStationDataFrame> rsdfFlux = Flux.fromIterable(rsdfs);

    Cd11ConnectionExchange connectionResponse = Cd11ConnectionExchange.builder()
      .setStationOrResponderName("TEST")
      .setStationOrResponderType("IDC")
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 0)
      .setServiceType("TCP")
      .setIpAddress(127001)
      .setPort(TEST_PORT)
      .build();

    willThrow(new IOException("can't connect"))
      .given(clientFactory)
      .tryRequestConnection(TEST_ADDRESS, TEST_PORT, failStation);
    willReturn(connectionResponse)
      .given(clientFactory)
      .tryRequestConnection(TEST_ADDRESS, TEST_PORT, passStation);
    willReturn(cd11Client)
      .given(clientFactory)
      .createCd11Client(any(), any(), any());

    given(cd11Client.connect()).willReturn(Mono.empty());

    StepVerifier.create(publisher.publish(() -> rsdfFlux))
      .verifyComplete();

    verify(clientFactory, times(2 + maxAttempts))
      .tryRequestConnection(anyString(), anyInt(), anyString());
    verify(clientFactory).createCd11Client(eq(passStation), any(), any());
    verify(clientFactory, never()).createCd11Client(eq(failStation), any(), any());
    verify(cd11Client).connect();
  }

  private RawStationDataFrame buildDummyRsdf(String stationName) {
    return RawStationDataFrame.builder()
      .generatedId()
      .setRawPayload(stationName.getBytes())
      .setMetadata(RawStationDataFrameMetadata.builder()
        .setAuthenticationStatus(AuthenticationStatus.AUTHENTICATION_SUCCEEDED)
        .setPayloadStartTime(Instant.EPOCH)
        .setPayloadEndTime(Instant.MAX)
        .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
        .setStationName(stationName)
        .setChannelNames(List.of("BHZ"))
        .setReceptionTime(Instant.now())
        .setWaveformSummaries(Map.of()).build())
      .build();
  }
}