package gms.shared.frameworks.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import gms.shared.frameworks.client.ServiceClientJdkHttp.BadRequest;
import gms.shared.frameworks.client.ServiceClientJdkHttp.ConnectionFailed;
import gms.shared.frameworks.client.ServiceClientJdkHttp.InternalServerError;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ServiceClientJdkHttpTests {

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper msgpackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  private static final String
    CLIENT_ERROR_PATH = "/client-error", CLIENT_ERROR_MSG = "client error!",
    SERVER_ERROR_PATH = "/server-error", SERVER_ERROR_MSG = "server error!";
  private static final Duration timeout = Duration.ofSeconds(5);
  private WireMockServer wireMockServer;
  private String baseUrl;

  private final RetryConfig basicSendRetryConfig = RetryConfig.create(50, 1000, ChronoUnit.MILLIS, 10);
  private final RetryConfig basicUpgradeRetryConfig = RetryConfig.create(50, 1000, ChronoUnit.MILLIS, 10);
  private final ServiceClientJdkHttp basicClient = ServiceClientJdkHttp.create(basicSendRetryConfig, basicUpgradeRetryConfig);

  private static final Type someObjectOptionalStringType;
  private static final Type fluxOfSomeObjectOptionalStringsType;

  static {
    final TypeFactory typeFactory = jsonMapper.getTypeFactory();

    someObjectOptionalStringType = typeFactory.constructParametricType(
      SomeObject.class, typeFactory.constructParametricType(Optional.class, String.class));
    fluxOfSomeObjectOptionalStringsType = TestUtilities
      .getResponseType(ServiceClientJdkHttpTests.class, "declareStreamingReturnType");
  }

  /**
   * Dummy operation that {@link TestUtilities#getResponseType(Class, String)} can use reflection on
   * to construct a Type corresponding to a "Flux<SomeObject<Optional<String></String>>>".
   */
  private static Flux<SomeObject<Optional<String>>> declareStreamingReturnType() {
    return Flux.empty();
  }

  @BeforeEach
  void setUp() {
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    wireMockServer.start();
    int port = wireMockServer.port();
    baseUrl = "http://localhost:" + port;
    mockServerError(CLIENT_ERROR_PATH, 400, CLIENT_ERROR_MSG);
    mockServerError(SERVER_ERROR_PATH, 500, SERVER_ERROR_MSG);
  }

  private void mockServerError(String url, int status, String responseMsg) {
    wireMockServer.givenThat(post(urlEqualTo(url))
      .willReturn(aResponse().withStatus(status).withBody(responseMsg)));
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void testCreateNoServer() {
    assertNotNull(ServiceClientJdkHttp.create(basicSendRetryConfig, basicUpgradeRetryConfig));
  }

  /**
   * Verifies non-streaming request and response can be serialized in each combination of formats.
   *
   * @param requestFormat {@link ContentType} of the request; cannot be a streaming format.
   */
  @ParameterizedTest
  @EnumSource(value = ContentType.class,
    names = {"JSON_STREAM", "MSGPACK_STREAM"},
    mode = Mode.EXCLUDE)
  void testRequestCustomObject(ContentType requestFormat) throws Exception {

    final Collection<ContentType> responseTypes = Arrays.stream(ContentType.values())
      .filter(c -> !ContentType.isStreaming(c))
      .collect(Collectors.toList());

    final String path = "/echo"; // server echoes the object back to you
    final SomeObject<Optional<String>> obj = SomeObject.create(5, "bar", Optional.of("foo"));

    for (ContentType responseFormat : responseTypes) {
      final StubMapping stub = mockServerSuccess(path, obj, obj, requestFormat, responseFormat);
      wireMockServer.addStubMapping(stub);

      final SomeObject<Optional<String>> returnObj = basicClient.sendSingleRequest(
        ServiceRequest.from(new URL(baseUrl + path), obj, timeout,
          someObjectOptionalStringType, requestFormat, responseFormat));
      assertEquals(obj, returnObj);

      // remove stub so the next iteration can't use it
      wireMockServer.removeStub(stub);
    }
  }

  @Test
  void testServerReturnsStatus4xxThrowsBadRequestWithBodyAsMsg() {
    assertRequestThrows(CLIENT_ERROR_PATH, BadRequest.class, CLIENT_ERROR_MSG);
  }

  @Test
  void testServerReturnsStatus5xxThrowsInternalServerErrorWithBodyAsMsg() {
    assertRequestThrows(SERVER_ERROR_PATH, InternalServerError.class, SERVER_ERROR_MSG);
  }

  private <T extends RuntimeException> void assertRequestThrows(
    String path, Class<T> exceptionClass, String msg) {

    assertEquals(msg, assertThrows(exceptionClass,
      () -> basicClient.sendSingleRequest(ServiceRequest.builder(
          new URL(baseUrl + path), "foo", timeout, String.class)
        .build()))
      .getMessage());
  }

  @ParameterizedTest
  @MethodSource("failsWithRetrySource")
  void testHttpUpgradeFailsWithRetry(
    Class<? extends Throwable> throwableClass) throws IOException, InterruptedException {
    String upgradePath = "/localhost/upgrade";
    String targetPath = "/localhost/foo";

    wireMockServer.givenThat(
      post(urlEqualTo(targetPath)).willReturn(aResponse().withStatus(200).withBody("foo")));

    // The wiremock server seems to not respond well to incredibly fast
    var httpClient = mock(HttpClient.class);
    given(httpClient.send(argThat((HttpRequest httpRequest) -> httpRequest.uri().toString().contains(upgradePath)), any()))
      .willThrow(throwableClass);

    var failClient = ServiceClientJdkHttp.create(httpClient, basicSendRetryConfig, basicUpgradeRetryConfig);

    ServiceRequest request = ServiceRequest
      .builder(new URL(baseUrl + "/localhost" + "/foo"), "foo", timeout, String.class).build();

    assertThrows(ConnectionFailed.class, () -> failClient.send(request));
    if (throwableClass.getTypeName().equals(InterruptedException.class.getName())) {
      assertTrue(Thread.interrupted());
    }
    wireMockServer.verify(0, postRequestedFor(urlEqualTo(targetPath)));
  }

  public static Stream<Arguments> failsWithRetrySource() {
    return Stream.of(
      Arguments.arguments(ConnectionFailed.class),
      Arguments.arguments(InterruptedException.class)
    );
  }

  @Test
  void testHttpUpgradeSucceeds() throws MalformedURLException, JsonProcessingException {
    String upgradePath = "/localhost/upgrade";
    String targetPath = "/localhost/foo";

    wireMockServer.givenThat(get(urlEqualTo(upgradePath))
      .willReturn(aResponse().withStatus(200).withBody("Upgraded")));

    wireMockServer.givenThat(post(urlEqualTo(targetPath))
      .willReturn(aResponse().withStatus(200).withBody(jsonMapper.writeValueAsString("foo"))));

    ServiceRequest request = ServiceRequest.builder(
      new URL(baseUrl + "/localhost" + "/foo"), "foo", timeout, String.class).build();
    var result = assertDoesNotThrow(() -> basicClient.send(request));
    wireMockServer.verify(1, getRequestedFor(urlEqualTo(upgradePath)));
    wireMockServer.verify(1, postRequestedFor(urlEqualTo(targetPath)));
  }

  private static StubMapping mockServerSuccess(String path, Object request, Object response,
    ContentType requestFormat, ContentType responseFormat)
    throws Exception {
    // taking the easy way out and doing everything as if there's only two ContentType's in this test
    final boolean requestMsgpack = requestFormat.equals(ContentType.MSGPACK);
    final boolean responseMsgpack = responseFormat.equals(ContentType.MSGPACK);
    final ContentPattern<?> pat = requestMsgpack ?
      binaryEqualTo(msgpackMapper.writeValueAsBytes(request))
      : equalTo(jsonMapper.writeValueAsString(request));
    MappingBuilder bob = accept(contentType(post(urlEqualTo(path)), requestFormat), responseFormat)
      .withRequestBody(pat);
    bob = responseMsgpack ?
      bob.willReturn(ok().withBody(msgpackMapper.writeValueAsBytes(response)))
      : bob.willReturn(ok().withBody(jsonMapper.writeValueAsString(response)));
    return bob.build();
  }

  private static MappingBuilder accept(MappingBuilder mb, ContentType format) {
    return mb.withHeader("Accept", containing(format.toString()));
  }

  private static MappingBuilder contentType(MappingBuilder mb, ContentType format) {
    return mb.withHeader("Content-Type", containing(format.toString()));
  }


  @ParameterizedTest
  @EnumSource(value = ContentType.class, names = {"JSON_STREAM", "MSGPACK_STREAM"})
  void testRequestStreamingResponse(ContentType responseFormat) throws Exception {

    final Collection<ContentType> requestTypes = Arrays.stream(ContentType.values())
      .filter(c -> !ContentType.isStreaming(c))
      .collect(Collectors.toList());

    final String path = "/streaming-response";
    final Integer request = 9;

    // Create a list containing the expected response body
    final List<SomeObject<Optional<String>>> expectedResponseList = IntStream.range(0, request)
      .mapToObj(i -> SomeObject.create(
        i * Math.PI,
        Double.toString(Math.exp(i)),
        Optional.of(Double.toString(Math.E * i))))
      .collect(Collectors.toList());

    // Serialize the response body into a byte[]
    final byte[] responseByteArray = TestUtilities
      .collectionToByteArray(expectedResponseList, responseFormat);

    for (ContentType requestFormat : requestTypes) {
      final StubMapping stub = mockChunkedResponse(path, request, responseByteArray, requestFormat,
        responseFormat);
      wireMockServer.addStubMapping(stub);

      final Flux<SomeObject<Optional<String>>> returnFlux = basicClient.sendSingleRequest(
        ServiceRequest.from(
          new URL(baseUrl + path),
          request,
          timeout,
          fluxOfSomeObjectOptionalStringsType,
          requestFormat,
          responseFormat)
      );

      // Verify the output Flux contains all of the expected elements in the correct order
      assertNotNull(returnFlux);
      final List<SomeObject<Optional<String>>> actualResponseList = new ArrayList<>();
      returnFlux.collectList().subscribe(actualResponseList::addAll).dispose();
      assertEquals(expectedResponseList, actualResponseList);

      // remove stub so the next iteration can't use it
      wireMockServer.removeStub(stub);
    }
  }

  private static StubMapping mockChunkedResponse(String path, Object request,
    byte[] serializedResponse, ContentType requestFormat, ContentType responseFormat)
    throws Exception {

    final ContentPattern<?> pat = requestFormat.equals(ContentType.MSGPACK)
      ? binaryEqualTo(msgpackMapper.writeValueAsBytes(request))
      : equalTo(jsonMapper.writeValueAsString(request));

    final int numChunks = 25;
    final int responseTimeMs = 1250;
    return post(urlEqualTo(path))
      .withHeader("Accept", containing(responseFormat.toString()))
      .withHeader("Content-Type", containing(requestFormat.toString()))
      .withRequestBody(pat)
      .willReturn(
        ok()
          .withChunkedDribbleDelay(numChunks, responseTimeMs)
          .withBody(serializedResponse)
      ).build();
  }
}
