package gms.shared.frameworks.client.generation;

import gms.shared.frameworks.client.ServiceClientJdkHttp;
import gms.shared.frameworks.client.ServiceRequest;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientGeneratorTests {

  private static final String NO_CONSUMES_OR_PRODUCES = "no-consumes-or-produces";
  private static final String CONSUMES_MSGPACK = "consumes-msgpack";
  private static final String PRODUCES_MSGPACK = "produces-msgpack";
  private static final String VOID_ROUTE = "void";
  private static final String STREAMING_ROUTE = "streaming";
  private static final String STREAMING_PRODUCES_MSGPACK_ROUTE = "streaming-msgpack";
  private static final String COMPONENT_NAME = "the-component";
  private static final String BASE_PATH = "example";
  private static final String HOSTNAME = "some-host";
  private static final int PORT = 5555;
  private static final Duration TIMEOUT = Duration.ofMillis(123);
  private static final String s = "foo";
  private static final Type typeOfS = s.getClass();
  private static final String baseUrl = String.format("http://%s:%d", HOSTNAME, PORT);
  @Mock
  private ServiceClientJdkHttp mockClient;
  @Mock
  private SystemConfig sysConfig;


  @Test
  void testCreateClientWithoutComponentAnnotationThrows() {
    given(sysConfig.getValueAsLong(argThat((String key) -> key != null && key.contains("initial-delay")))).willReturn(1L);
    given(sysConfig.getValueAsLong(argThat((String key) -> key != null && key.contains("max-delay")))).willReturn(2L);
    given(sysConfig.getValue((argThat((String key) -> key != null && key.contains("delay-units"))))).willReturn("MILLIS");
    given(sysConfig.getValueAsInt(argThat((String key) -> key != null && key.contains("max-attempts")))).willReturn(1);

    assertEquals("Client interface must have @Component",
      assertThrows(IllegalArgumentException.class,
        () -> ClientGenerator.createClient(InterfaceWithoutComponentAnnotation.class, sysConfig))
        .getMessage());
  }

  @Test
  void testCreateClientWithEmptyBasePath() throws Exception {
    ServiceRequest expectedRequest = ServiceRequest.from(
      new URL(String.format("%s/%s", baseUrl, NO_CONSUMES_OR_PRODUCES)),
      s, TIMEOUT, typeOfS, ContentType.defaultContentType(), ContentType.defaultContentType());

    given(sysConfig.getUrlOfComponent(COMPONENT_NAME)).willReturn((new URL(baseUrl)));
    given(sysConfig.getValueAsDuration(SystemConfig.CLIENT_TIMEOUT)).willReturn(TIMEOUT);

    ExampleApiWithEmptyBasePath apiWithEmptyBasePath = ClientGenerator.createClient(
      ExampleApiWithEmptyBasePath.class, sysConfig, mockClient);

    assertNotNull(apiWithEmptyBasePath);
    apiWithEmptyBasePath.noConsumesOrProduces(s);
    verify(mockClient).send(expectedRequest);
  }

  @ParameterizedTest
  @MethodSource("clientCalls")
  void testClientCalls(Consumer<ExampleApi> apiCall, ServiceRequest expectedRequest) throws Exception {
    given(sysConfig.getUrlOfComponent(COMPONENT_NAME)).willReturn(new URL(baseUrl));
    given(sysConfig.getValueAsDuration(SystemConfig.CLIENT_TIMEOUT)).willReturn(TIMEOUT);
    var api = ClientGenerator.createClient(ExampleApi.class, sysConfig, mockClient);

    apiCall.accept(api);
    verify(mockClient).send(expectedRequest);
  }

  static Stream<Arguments> clientCalls() throws Exception {
    return Stream.of(
      arguments(call((ExampleApi api) -> api.noConsumesOrProduces(s)),
        expectedRequest(NO_CONSUMES_OR_PRODUCES, ContentType.defaultContentType(), ContentType.defaultContentType())),
      arguments(call((ExampleApi api) -> api.consumesMsgpack(s)),
        expectedRequest(CONSUMES_MSGPACK, ContentType.MSGPACK, ContentType.defaultContentType())),
      arguments(call((ExampleApi api) -> api.producesMsgpack(s)),
        expectedRequest(PRODUCES_MSGPACK, ContentType.defaultContentType(), ContentType.MSGPACK)),
      arguments(call((ExampleApi api) -> api.voidRoute(s)),
        expectedRequest(VOID_ROUTE, ContentType.defaultContentType(), ContentType.defaultContentType())),
      arguments(call((ExampleApi api) -> api.streamingRoute(s)),
        ServiceRequest.from(constructUrl(STREAMING_ROUTE), s, TIMEOUT,
          ExampleApi.class.getMethod("streamingRoute", String.class).getGenericReturnType(),
          ContentType.defaultContentType(), ContentType.defaultStreamingContentType())),
      arguments(call((ExampleApi api) -> api.streamingMsgPackRoute(s)),
        ServiceRequest.from(constructUrl(STREAMING_PRODUCES_MSGPACK_ROUTE), s, TIMEOUT,
          ExampleApi.class.getMethod("streamingMsgPackRoute", String.class).getGenericReturnType(),
          ContentType.defaultContentType(), ContentType.MSGPACK_STREAM
        )));
  }

  private static Consumer<ExampleApi> call(Consumer<ExampleApi> exampleApiConsumer) {
    return exampleApiConsumer;
  }

  private static ServiceRequest expectedRequest(String expectedPath, ContentType requestFormat,
    ContentType responseFormat) throws MalformedURLException {
    return ServiceRequest.from(constructUrl(expectedPath), s, TIMEOUT, typeOfS, requestFormat, responseFormat);
  }

  private static URL constructUrl(String path) throws MalformedURLException {
    return new URL(String.format("%s/%s/%s", baseUrl, BASE_PATH, path));
  }

  @Component(COMPONENT_NAME)
  @Path(BASE_PATH)
  interface ExampleApi {

    @Path(NO_CONSUMES_OR_PRODUCES)
    @POST
    String noConsumesOrProduces(String s);

    @Path(CONSUMES_MSGPACK)
    @POST
    @Consumes({ContentType.MSGPACK_NAME})
    String consumesMsgpack(String s);

    @Path(PRODUCES_MSGPACK)
    @POST
    @Produces({ContentType.MSGPACK_NAME})
    String producesMsgpack(String s);

    @Path(VOID_ROUTE)
    @POST
    void voidRoute(String s);

    @Path(STREAMING_ROUTE)
    @POST
    Flux<String> streamingRoute(String s);

    @Path(STREAMING_PRODUCES_MSGPACK_ROUTE)
    @POST
    @Produces({ContentType.MSGPACK_STREAM_NAME})
    Flux<String> streamingMsgPackRoute(String s);

  }

  @Path(BASE_PATH)
  interface InterfaceWithoutComponentAnnotation {

    @Path(NO_CONSUMES_OR_PRODUCES)
    @POST
    String noConsumesOrProduces(String s);

    @Path(CONSUMES_MSGPACK)
    @POST
    String consumesMsgpack(String s);

    @Path(PRODUCES_MSGPACK)
    @POST
    String producesMsgpack(String s);

  }

  @Component(COMPONENT_NAME)
  @Path("")
  interface ExampleApiWithEmptyBasePath {

    @Path(NO_CONSUMES_OR_PRODUCES)
    @POST
    String noConsumesOrProduces(String s);

    @Path(CONSUMES_MSGPACK)
    @POST
    @Consumes({ContentType.MSGPACK_NAME})
    String consumesMsgpack(String s);

    @Path(PRODUCES_MSGPACK)
    @POST
    @Produces({ContentType.MSGPACK_NAME})
    String producesMsgpack(String s);
  }

}
