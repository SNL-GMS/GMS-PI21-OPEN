package gms.shared.frameworks.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.common.GmsCommonRoutes;
import gms.shared.frameworks.common.HttpStatus;
import gms.shared.frameworks.common.HttpStatus.Code;
import gms.shared.frameworks.common.config.ServerConfig;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpServiceTests {

  private static final Set<Route> routes = Set.of(
    // returns the body of the request unchanged.
    // Purposely leave out leading '/' to see that it gets setup correctly anyway.
    Route.create("echoBody", (req, deserializer) -> Response.success(req.getBody())),

    // returns the keys from body of the request as an array
    Route.create("/echoKeys", HttpServiceTests::echoKeys),

    // returns a Flux based on parameters from the StreamingRequest input
    Route.create("/streamingResponse", HttpServiceTests::streamingResponse),

    // returns an error
    Route.create("/error", (req, deserializer) -> Response.error(Code.BAD_GATEWAY, "error")),

    // a route that just always throws an exception
    Route.create("/throw", (req, deser) -> {
      throw new RuntimeException("psyche!");
    }),

    // asserts the route is called on the elastic bounded threadpool
    Route.create("/returnThreadPool",
      (req, deser) -> Response.success(Thread.currentThread().getName().toLowerCase()))
  );

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper msgpackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();
  private static final HttpClient client = HttpClient.newBuilder()
    .executor(Executors.newFixedThreadPool(10))
    .priority(1)
    .build();

  private static int servicePort;

  private static HttpService service;

  @BeforeAll
  static void setup() throws Exception {
    servicePort = getAvailablePort();
    final ServiceDefinition def = ServiceDefinition.builder(
        ServerConfig.from(servicePort, 10, 20, Duration.ofMillis(100)), "test")
      .setRoutes(routes).build();
    assertServiceIsUnreachable();
    service = new HttpService(def);
    service.start();
    assertTrue(service.isRunning());
    assertEquals(def, service.getDefinition());
    assertDoesNotThrow(HttpServiceTests::upgradeToHttp2);
  }

  private static int getAvailablePort() throws Exception {
    var ephemeralServerSocket = new ServerSocket(0);
    final var port = ephemeralServerSocket.getLocalPort();
    ephemeralServerSocket.close();
    return port;
  }

  private static void assertServiceIsUnreachable() {
    try {
      var httpResponse = requestEchoBodyRoute("foo");
//      fail("Expected to throw exception by trying to connect to "
//          + "service when it's supposed to be unreachable");
      assertTrue(httpResponse.body().isEmpty());
    } catch (Exception ex) {
      // do nothing; expected to throw a unirest exception
    }
  }

  private static void upgradeToHttp2() {
    try {
      client.send(HttpRequest.newBuilder(
          URI.create("http://localhost:" + servicePort + "/test" + GmsCommonRoutes.CONNECTION_UPGRADE_PATH))
        .GET()
        .build(), BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("Could not upgrade to HTTP/2", e);
    }
  }

  @AfterAll
  static void teardown() {
    service.stop();
    assertFalse(service.isRunning());
    // call stop again to check that doesn't throw an exception
    service.stop();
  }

  private static Response echoKeys(Request request, ObjectMapper deserializer) {
    try {
      // exercise the functionality to get headers
      var requestHeaderMap = request.getHeaders();
      assertNotNull(requestHeaderMap);
      // assert there are at least some headers;
      // hard to rely on specific ones because it depends on the request
      assertFalse(requestHeaderMap.isEmpty());
      var jsonNode = deserializer.readTree(request.getRawBody());
      var keysList = new ArrayList<>();
      jsonNode.fieldNames().forEachRemaining(keysList::add);
      return Response.success(keysList);
    } catch (IOException e) {
      return Response.clientError("Exception on deserialization: " + e.getMessage());
    }
  }

  private static HttpResponse<String> requestEchoBodyRoute(String body)
    throws Exception {
    return client.send(HttpRequest
      .newBuilder(URI.create("http://localhost:" + servicePort + "/echoBody"))
      .header("Content-Type", ContentType.JSON_NAME)
      .header("Accept", ContentType.JSON_NAME)
      .POST(BodyPublishers.ofString(body))
      .build(), BodyHandlers.ofString());
  }

  private static Response<Flux<?>> streamingResponse(Request request, ObjectMapper deserializer) {
    try {
      // exercise the functionality to get headers
      var headers = request.getHeaders();
      assertNotNull(headers);
      // assert there are at least some headers;
      // hard to rely on specific ones because it depends on the request
      assertFalse(headers.isEmpty());

      final var streamingRequest = deserializer
        .readValue(request.getBody(), StreamingRequest.class);
      return Response.success(Flux.range(0, streamingRequest.getTotaElements())
        .map(i -> {
          if (i >= (streamingRequest.getTotaElements() - streamingRequest.getBadElements())) {
            // Return an object that can't be serialized correctly
            return new HttpServiceTests();
          } else {
            return streamingRequest.getElementCreationClosure().apply(i);
          }
        })
      );

    } catch (IOException e) {
      return Response.clientError("Exception on deserialization: " + e.getMessage());
    }
  }

  /**
   * Uses the provided {@link ObjectMapper} to deserialize the {@link InputStream} into a List of
   * t{@link ThreeValues} objects. Elements in the input stream are individually serialized (i.e.
   * they are not serialized as an array or a collection).
   *
   * @param objectMapper {@link ObjectMapper}, not null
   * @param inputStream {@link InputStream}, not null
   * @param elementType {@link Class} type of the deserialized elements
   * @return List of ThreeValue elements
   * @throws IOException if there is a problem deserializing the elements
   */
  private static <T> List<T> readCollection(ObjectMapper objectMapper, InputStream inputStream,
    Class<T> elementType) throws IOException {

    final var parser = objectMapper.getFactory().createParser(inputStream);

    List<T> output = new ArrayList<>();
    try {
      while (!parser.isClosed() && parser.nextToken() != null) {
        output.add(parser.readValueAs(elementType));
      }
    } catch (JsonEOFException ex) {
      // MessagePack JsonParser implementation throws this when EOF is reached in nextToken()
    }

    return output;
  }

  private static HttpResponse<String> requestEchoKeysRouteMsgpack(byte[] body) throws Exception {
    return client
      .send(HttpRequest.newBuilder(URI.create("http://localhost:" + servicePort + "/echoKeys"))
          .header("Content-Type", ContentType.MSGPACK_NAME)
          .POST(BodyPublishers.ofByteArray(body, 0, body.length))
          .build()
        , BodyHandlers.ofString());
  }

  private static HttpResponse<String> requestEchoKeysRoute_DuplicateHeader(String body) throws Exception {
    return client.send(HttpRequest
      .newBuilder(URI.create("http://localhost:" + servicePort + "/echoKeys"))
      .header("Content-Type", ContentType.JSON_NAME)
      .header("Accept-Encoding", "None")
      .header("Accept-Encoding", "deflate")
      .POST(BodyPublishers.ofString(body))
      .build(), BodyHandlers.ofString());
  }

  private static HttpResponse<String> requestEchoKeysRoute(String body) throws Exception {
    return client.send(HttpRequest
      .newBuilder(URI.create("http://localhost:" + servicePort + "/echoKeys"))
      .setHeader("Content-Type", ContentType.JSON_NAME)
      .POST(BodyPublishers.ofString(body))
      .build(), BodyHandlers.ofString());
  }

  private static HttpResponse<String> requestThrowRoute() throws Exception {
    return client.send(HttpRequest.newBuilder(
          URI.create("http://localhost:" + servicePort + "/throw"))
        .POST(BodyPublishers.noBody())
        .build()
      , BodyHandlers.ofString());
  }

  private static HttpResponse<String> requestReturnThreadPoolRoute() throws Exception {
    return client.send(HttpRequest.newBuilder(
          URI.create("http://localhost:" + servicePort + "/returnThreadPool"))
        .POST(BodyPublishers.noBody())
        .build()
      , BodyHandlers.ofString());
  }

  private static HttpResponse<InputStream> requestStreamingResponseRoute(
    StreamingRequest request, ContentType acceptType) throws Exception {
    return client.send(HttpRequest
        .newBuilder(URI.create("http://localhost:" + servicePort + "/streamingResponse"))
        .header("Content-Type", ContentType.JSON_NAME)
        .header("Accept", acceptType.toString())
        .POST(BodyPublishers.ofString(jsonMapper.writeValueAsString(request))).build(),
      BodyHandlers.ofInputStream());
  }

  private static void verifyExpectedStatusAndContentType(HttpResponse<InputStream> response,
    ContentType expected) {
    assertNotNull(response.body());
    assertEquals(HttpStatus.OK_200, response.statusCode());

    final Optional<String> responseContentType = response.headers().firstValue("Content-Type");
    assertTrue(responseContentType.isPresent());
    assertEquals(expected.toString(), responseContentType.get());
  }

  private static void verifyExpectedCollection(StreamingRequest request, InputStream actualBody,
    ObjectMapper mapper) throws IOException {

    final int numExpected = request.getTotaElements() - request.getBadElements();

    assertNotNull(actualBody);

    final Class<?> responseClass =
      request.getReturnThreeValuesElements() ? ThreeValues.class : Integer.class;
    final List<?> actual = readCollection(mapper, actualBody, responseClass);

    final IntFunction<?> expectedElementClosure = request.getElementCreationClosure();
    final List<?> expected = IntStream.range(0, numExpected)
      .mapToObj(expectedElementClosure)
      .collect(Collectors.toList());

    assertNotNull(actual);
    assertEquals(numExpected, actual.size());
    assertEquals(expected, actual);
  }

  @Test
  void testServiceStartAgain() {
    assertEquals("Service is already running",
      assertThrows(IllegalStateException.class,
        () -> service.start()).getMessage());
  }

  @Test
  void testConstructorNullCheck() {
    assertEquals("Cannot create HttpService will null definition",
      assertThrows(NullPointerException.class,
        () -> new HttpService(null)).getMessage());
  }

  @Test
  void testEchoBodyRoute() throws Exception {
    // test 'echo body' route with JSON
    final var bodyString = "a body";
    var httpResponse = requestEchoBodyRoute(bodyString);
    assertNotNull(httpResponse.body());
    assertEquals(HttpStatus.OK_200, httpResponse.statusCode());
    assertTrue(httpResponse.headers().firstValue("Content-Type").isPresent());
    assertEquals(ContentType.JSON_NAME, httpResponse.headers().firstValue("Content-Type").get());
    // response body is wrapped in quotes because of how JSON serialization works
    assertEquals("\"" + bodyString + "\"", httpResponse.body());

    var request = HttpRequest
      .newBuilder(URI.create("http://localhost:" + servicePort + "/echoBody"))
      .header("Content-Type", ContentType.JSON_NAME)
      .header("Accept", ContentType.MSGPACK_NAME)
      .POST(BodyPublishers.ofString(bodyString))
      .build();
    var msgResponse = client.send(request, BodyHandlers.ofByteArray());
    assertNotNull(msgResponse.body());
    assertEquals(HttpStatus.OK_200, msgResponse.statusCode());
    assertTrue(msgResponse.headers().firstValue("Content-Type").isPresent());
    assertEquals(ContentType.MSGPACK_NAME, msgResponse.headers().firstValue("Content-Type").get());
    final var expectedByteArray = msgpackMapper.writeValueAsBytes(bodyString);
    assertArrayEquals(expectedByteArray, msgResponse.body());
  }

  @Test
  void testEchoKeysRouteJson() throws Exception {
    final var m = Map.of("key1", "val1", "key2", "val2");
    final var httpResponse = requestEchoKeysRoute(jsonMapper.writeValueAsString(m));
    assertNotNull(httpResponse.body());
    assertEquals(HttpStatus.OK_200, httpResponse.statusCode());
    final var responseKeysArray = jsonMapper.readValue(httpResponse.body(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeysArray)));
  }

  @Test
  void testErrorRoute() throws Exception {
    var response = HttpClient.newHttpClient()
      .send(HttpRequest.newBuilder(URI.create("http://localhost:" + servicePort
        + "/error")).POST(BodyPublishers.noBody()).build(), BodyHandlers.ofString());

    assertNotNull(response.body());
    assertEquals(Code.BAD_GATEWAY.getStatusCode(), response.statusCode());
    assertEquals("error", response.body());
    assertTrue(response.headers().firstValue("Content-Type").isPresent());
    assertEquals("text/plain", response.headers().firstValue("Content-Type").get());
  }

  @Test
  void testEchoKeysRouteJson_DuplicateHeaders() throws Exception {
    assertTrue(service.isRunning(), "Service should be running");
    final var requestBodyDataMap = Map.of("key1", "val1", "key2", "val2");
    final var response = requestEchoKeysRoute_DuplicateHeader(jsonMapper.writeValueAsString(requestBodyDataMap));
    assertNotNull(response.body());
    assertEquals(HttpStatus.OK_200, response.statusCode());
    final var responseKeysArray = jsonMapper.readValue(response.body(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeysArray)));
  }

  @Test
  void testEchoKeysRouteMsgpack() throws Exception {
    final var requestBodyDataMap = Map.of("key1", "val1", "key2", "val2");
    final var response = requestEchoKeysRouteMsgpack(
      msgpackMapper.writeValueAsBytes(requestBodyDataMap));
    assertNotNull(response.body());
    assertEquals(HttpStatus.OK_200, response.statusCode());
    final var responseKeysArray = jsonMapper.readValue(response.body(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeysArray)));
  }

  @Test
  void testThrowRoute() throws Exception {
    final var httpResponse = requestThrowRoute();
    assertEquals(Code.INTERNAL_SERVER_ERROR.getStatusCode(), httpResponse.statusCode());
    assertTrue(httpResponse.body().contains("psyche"),
      "Expected response to contain error message ('psyche')");
  }

  @Test
  void testReturnThreadPoolRoute() throws Exception {
    final var response = requestReturnThreadPoolRoute();
    assertEquals(HttpStatus.OK_200, response.statusCode());

    final String threadNameLower = response.body().toLowerCase();
    assertAll(
      () -> assertTrue(threadNameLower.contains("bounded"),
        "Expected thread name to contain 'bounded'"),
      () -> assertTrue(threadNameLower.contains("elastic"),
        "Expected thread name to contain 'elastic'")
    );
  }

  @Test
  void testStreamingIntegersJsonResponseRoute() throws Exception {
    final StreamingRequest streamingRequest = StreamingRequest.create(2002, 0, false);
    final ContentType contentType = ContentType.JSON_STREAM;
    final HttpResponse<InputStream> response = requestStreamingResponseRoute(streamingRequest,
      contentType);

    verifyExpectedStatusAndContentType(response, contentType);
    verifyExpectedCollection(streamingRequest, response.body(), jsonMapper);
  }

  @Test
  void testStreamingIntegersMsgPackResponseRoute() throws Exception {
    final StreamingRequest streamingRequest = StreamingRequest.create(1002, 0, false);
    final ContentType contentType = ContentType.MSGPACK_STREAM;
    final HttpResponse<InputStream> response = requestStreamingResponseRoute(streamingRequest,
      contentType);

    verifyExpectedStatusAndContentType(response, contentType);
    verifyExpectedCollection(streamingRequest, response.body(), msgpackMapper);
  }

  @Test
  void testServiceMakesHealthcheckRouteJson() throws Exception {
    final var httpRequest = HttpRequest.newBuilder(
        URI.create("http://localhost:" + servicePort + "/test" + GmsCommonRoutes.HEALTHCHECK_PATH))
      .GET()
      .header(String.valueOf(HttpHeaderNames.CONTENT_TYPE), ContentType.JSON_NAME)
      .build();
    final var httpResponse = HttpClient.newHttpClient()
      .send(httpRequest, BodyHandlers.ofString());
    assertEquals(Code.OK.getStatusCode(), httpResponse.statusCode());
    assertTrue(httpResponse.body().contains("\"aliveAt\""));
  }

  @Test
  void testStreamingJsonResponseRoute() throws Exception {
    final var streamingRequest = StreamingRequest.create(13, 0, true);
    final var contentType = ContentType.JSON_STREAM;
    final var streamHttpResponse = requestStreamingResponseRoute(streamingRequest,
      contentType);

    verifyExpectedStatusAndContentType(streamHttpResponse, contentType);
    verifyExpectedCollection(streamingRequest, streamHttpResponse.body(), jsonMapper);
  }

  @Test
  void testStreamingMsgPackResponseRoute() throws Exception {
    final var streamingRequest = StreamingRequest.create(23, 0, true);
    final var contentType = ContentType.MSGPACK_STREAM;
    final var streamHttpResponse = requestStreamingResponseRoute(streamingRequest,
      contentType);

    verifyExpectedStatusAndContentType(streamHttpResponse, contentType);
    verifyExpectedCollection(streamingRequest, streamHttpResponse.body(), msgpackMapper);
  }

  @Test
  void testStreamingJsonResponseBadElementsExpectResponseWithOtherElements() throws Exception {
    final var streamingRequest = StreamingRequest.create(21, 6, true);
    final var contentType = ContentType.JSON_STREAM;
    final var streamHttpResponse = requestStreamingResponseRoute(streamingRequest,
      contentType);

    verifyExpectedStatusAndContentType(streamHttpResponse, contentType);
    verifyExpectedCollection(streamingRequest, streamHttpResponse.body(), jsonMapper);
  }

  @Test
  void testStreamingMsgPackResponseBadElementsExpectResponseWithOtherElements() throws Exception {
    final var streamingRequest = StreamingRequest.create(99, 27, true);
    final var contentType = ContentType.MSGPACK_STREAM;
    final var streamHttpResponse = requestStreamingResponseRoute(streamingRequest,
      contentType);

    verifyExpectedStatusAndContentType(streamHttpResponse, contentType);
    verifyExpectedCollection(streamingRequest, streamHttpResponse.body(), msgpackMapper);
  }

  /**
   * Utility class used as the response element type for the streaming response service
   */
  @AutoValue
  static abstract class ThreeValues {

    public abstract int getA();

    public abstract double getB();

    public abstract String getC();

    @JsonCreator
    static ThreeValues create(
      @JsonProperty("a") int a,
      @JsonProperty("b") double b,
      @JsonProperty("c") String c) {

      return new AutoValue_HttpServiceTests_ThreeValues(a, b, c);
    }

    static ThreeValues create(int seed) {
      return ThreeValues.create(seed * 2, Math.exp(seed * Math.PI), "SEED:" + seed);
    }
  }

  /**
   * Utility class describing the request body for the streaming response tests
   */
  @AutoValue
  static abstract class StreamingRequest {

    public abstract int getTotaElements();

    public abstract int getBadElements();

    public abstract boolean getReturnThreeValuesElements();

    @JsonCreator
    static StreamingRequest create(
      @JsonProperty("totalElements") int totalElements,
      @JsonProperty("badElements") int badElements,
      @JsonProperty("returnThreeValuesElements") boolean returnThreeValuesElements) {

      return new AutoValue_HttpServiceTests_StreamingRequest(totalElements, badElements,
        returnThreeValuesElements);
    }

    @Memoized
    IntFunction<?> getElementCreationClosure() {
      return getReturnThreeValuesElements()
        ? ThreeValues::create
        : i -> i;
    }
  }
}
