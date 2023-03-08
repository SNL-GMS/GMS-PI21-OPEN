package gms.shared.frameworks.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.Step;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamingResponseDeserializerTests {

  private final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private final Type responseType = TestUtilities.getResponseType(
    StreamingResponseDeserializerTests.class, "declareReturnType");
  private final InputStream inputStream =
    TestUtilities.collectionToInputStream(List.of(), ContentType.JSON_STREAM);

  /*
   * Dummy operations that TestUtilities#getResponseType(Class, String) can use reflection on
   * to construct a Type objects.
   */

  @SuppressWarnings("unused")
  private Flux<SomeObject<int[]>> declareReturnType() {
    return Flux.empty();
  }

  @SuppressWarnings("unused")
  private Flux<SomeObject<int[]>> declareSomeObjectIntArrayElementType() {
    return Flux.empty();
  }

  @SuppressWarnings("unused")
  private List<SomeObject<Optional<String>>> declareListType() {
    return List.of(SomeObject.create(1.0, "", Optional.of("")));
  }

  @SuppressWarnings("unused")
  private Flux<?> declareWildcardElementType() {
    return Flux.empty();
  }

  @SuppressWarnings("unused")
  private <T> Flux<T> declareParameterizedElementType() {
    return Flux.empty();
  }

  @SuppressWarnings("unused")
  private Flux<String> declareStringElementType() {
    return Flux.empty();
  }

  @SuppressWarnings("unused")
  private Flux<Integer> declareIntegerElementType() {
    return Flux.empty();
  }

  private static Type getType(String method) {
    return TestUtilities.getResponseType(StreamingResponseDeserializerTests.class, method);
  }

  @Test
  void testCreate() {
    assertNotNull(StreamingResponseDeserializer.create(mapper));
  }

  @Test
  void testReadToFluxRequiresNonWildcardElementType() {
    assertException(
      () -> StreamingResponseDeserializer.create(mapper)
        .readToFlux(inputStream, getType("declareWildcardElementType")),
      IllegalArgumentException.class,
      "responseType must be a Flux of a concrete type but is a Flux<?>");
  }

  @Test
  void testReadToFluxRequiresNonTypeParameterElementType() {
    assertException(
      () -> StreamingResponseDeserializer.create(mapper)
        .readToFlux(inputStream, getType("declareParameterizedElementType")),
      IllegalArgumentException.class,
      "responseType must be a Flux of a concrete type but is a Flux<T>");
  }

  @Test
  void testReadToFluxRequiresResponseTypeIsAParameterizedType() {
    assertException(() -> StreamingResponseDeserializer.create(mapper)
        .readToFlux(inputStream, Double.class),
      IllegalArgumentException.class,
      "responseType must be a Flux but can't be as it is not a ParameterizedType");
  }

  @Test
  void testReadToFluxRequiresResponseTypeIsAFlux() {
    assertException(() -> StreamingResponseDeserializer.create(mapper)
        .readToFlux(inputStream, getType("declareListType")),
      IllegalArgumentException.class, "responseType must be a Flux");
  }

  @Test
  void testCreateValidatesInputsNotNull() {
    assertNullPointerException(() ->
      StreamingResponseDeserializer.create(null), "ObjectMapper can't be null");
  }

  @ParameterizedTest
  @EnumSource(value = ContentType.class, names = {"JSON_STREAM", "MSGPACK_STREAM"})
  void testReadToFlux(ContentType contentType) {
    final List<String> expected = IntStream.range(0, 25)
      .mapToObj(Integer::toString)
      .collect(Collectors.toList());

    final StreamingResponseDeserializer deserializer =
      StreamingResponseDeserializer.create(TestUtilities.contentTypeToMapper(contentType));

    // Verify the response Flux matches the expected Flux element for element
    Flux<?> actualResponse = deserializer.readToFlux(
      TestUtilities.collectionToInputStream(expected, contentType),
      getType("declareStringElementType"));
    assertNotNull(actualResponse);

    Step<?> verifier = StepVerifier.create(actualResponse);
    for (String expectedElement : expected) {
      verifier = verifier.assertNext(actualElement -> assertEquals(expectedElement, actualElement));
    }
    verifier.verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(value = ContentType.class, names = {"JSON_STREAM", "MSGPACK_STREAM"})
  void testReadIntegersToFlux(ContentType contentType) {
    final List<Integer> expected = IntStream.range(0, 1358).boxed().collect(Collectors.toList());

    final StreamingResponseDeserializer deserializer =
      StreamingResponseDeserializer.create(TestUtilities.contentTypeToMapper(contentType));

    // Verify the response Flux matches the expected Flux element for element
    Flux<?> actualResponse = deserializer.readToFlux(
      TestUtilities.collectionToInputStream(expected, contentType),
      getType("declareIntegerElementType"));
    assertNotNull(actualResponse);

    Step<?> verifier = StepVerifier.create(actualResponse);
    for (Integer expectedElement : expected) {
      verifier = verifier.assertNext(actualElement -> assertEquals(expectedElement, actualElement));
    }
    verifier.verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(value = ContentType.class, names = {"JSON_STREAM", "MSGPACK_STREAM"})
  void testReadToFluxSkipsBadElements(ContentType contentType) throws IOException {
    final SomeObject<int[]> a = SomeObject.create(1.1, "first", new int[]{1});
    final SomeObject<int[]> b = SomeObject.create(2.2, "second", new int[]{1, 2});

    final List<?> inputList = List.of(a, "bad element", b);

    final InputStream inputStreamSpy = spy(
      TestUtilities.collectionToInputStream(inputList, contentType));
    final StreamingResponseDeserializer deserializer =
      StreamingResponseDeserializer.create(TestUtilities.contentTypeToMapper(contentType));

    final Flux<SomeObject<int[]>> flux = deserializer
      .readToFlux(inputStreamSpy, getType("declareSomeObjectIntArrayElementType"));
    StepVerifier.create(flux)
      .assertNext(actual -> assertSameObject(a, actual))
      .assertNext(actual -> assertSameObject(b, actual))
      .verifyComplete();

    verify(inputStreamSpy, atLeastOnce()).close();
  }

  private static void assertSameObject(SomeObject<int[]> a, SomeObject<int[]> b) {
    assertEquals(a.getName(), b.getName());
    assertEquals(a.getNum(), b.getNum());
    assertArrayEquals(a.getGeneric(), b.getGeneric());
  }

  @Test
  void testMapperErrorEmitsFluxError() throws IOException {

    ObjectMapper mapperMock = mock(ObjectMapper.class);
    JsonFactory jsonFactoryMock = mock(JsonFactory.class);

    final InputStream inputStreamSpy =
      spy(TestUtilities.collectionToInputStream(List.of("one", "two"), ContentType.JSON_STREAM));

    when(mapperMock.getFactory()).thenReturn(jsonFactoryMock);
    when(mapperMock.getTypeFactory()).thenReturn(mapper.getTypeFactory());
    when(jsonFactoryMock.createParser((InputStream) any())).thenThrow(IOException.class);

    final StreamingResponseDeserializer deserializer = StreamingResponseDeserializer
      .create(mapperMock);

    // Read the flux
    StepVerifier
      .create(deserializer.readToFlux(inputStreamSpy, getType("declareStringElementType")))
      .expectErrorSatisfies(error -> {
        assertTrue(IOException.class.isAssignableFrom(error.getClass()));
        assertTrue(
          error.getMessage().contains("Error parsing InputStream when emitting to Flux"));
      })
      .verify();

    verify(inputStreamSpy, atLeastOnce()).close();
  }

  @Test
  void testReadToFluxClosesInputStream() throws IOException {

    final int numElements = 19;

    final StreamingResponseDeserializer deserializer = StreamingResponseDeserializer.create(mapper);

    final InputStream inputStreamSpy = spy(TestUtilities.collectionToInputStream(
      IntStream.range(0, numElements).mapToObj(Integer::toString).collect(Collectors.toList()),
      ContentType.JSON_STREAM));

    // Read the flux
    StepVerifier
      .create(deserializer.readToFlux(inputStreamSpy, getType("declareStringElementType")))
      .expectNextCount(numElements)
      .verifyComplete();

    verify(inputStreamSpy, atLeastOnce()).close();
  }

  @Test
  void testReadToFluxValidatesParameters() {
    assertAll(
      () -> assertNullPointerException(
        () -> StreamingResponseDeserializer.create(mapper).readToFlux(null, responseType),
        "inputStream can't be null"),

      () -> assertNullPointerException(
        () -> StreamingResponseDeserializer.create(mapper).readToFlux(inputStream, null),
        "fluxType can't be null")
    );
  }

  private void assertNullPointerException(Executable executable, String message) {
    assertException(executable, NullPointerException.class, message);
  }

  private void assertException(Executable executable, Class<? extends Throwable> exceptionClass,
    String message) {

    final Throwable throwable = assertThrows(exceptionClass, executable);
    assertEquals(message, throwable.getMessage());
  }
}
