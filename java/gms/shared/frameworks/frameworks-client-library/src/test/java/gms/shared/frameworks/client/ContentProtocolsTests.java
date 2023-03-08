package gms.shared.frameworks.client;

import gms.shared.frameworks.common.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.Step;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentProtocolsTests {

  @ParameterizedTest
  @EnumSource(ContentType.class)
  void testFromReturnsContentProtocolForEachContentType(ContentType contentType) {
    assertDoesNotThrow(() -> ContentProtocols.from(contentType));
    assertNotNull(ContentProtocols.from(contentType));
  }

  @Test
  void testFromNullContentTypeExpectNullPointerException() {
    assertTrue(
      assertThrows(NullPointerException.class,
        () -> ContentProtocols.from(null)
      ).getMessage().contains("ContentType can't be null"));
  }

  @ParameterizedTest
  @EnumSource(value = ContentType.class, names = {"JSON_STREAM", "MSGPACK_STREAM"})
  void testResponseStreamDeserializesToFlux(ContentType responseContentType) throws Exception {
    final ContentProtocol<String, InputStream> jsonStream =
      ContentProtocols.from(responseContentType);

    final List<SomeObject<int[]>> responseList = createResponseList(20);
    final InputStream responseBody = new ByteArrayInputStream(
      TestUtilities.collectionToByteArray(responseList, responseContentType));

    // In the generated client the response type comes from the annotated method's return type,
    // so in this example it would be "Flux<SomeObject<int[]>>"
    final Type responseType = TestUtilities
      .getResponseType(ContentProtocolsTests.class, "declareReturnType");
    assertNotNull(responseType);

    // Verify the response Flux matches the expected Flux element for element
    Flux<SomeObject<int[]>> actualResponse = jsonStream.deserialize(responseBody, responseType);
    assertNotNull(actualResponse);

    Step<SomeObject<int[]>> verifier = StepVerifier.create(actualResponse);
    for (SomeObject<int[]> expected : responseList) {
      verifier = verifier.assertNext(actual -> assertSameObject(expected, actual));
    }
    verifier.verifyComplete();
  }

  private static void assertSameObject(SomeObject<int[]> a, SomeObject<int[]> b) {
    assertEquals(a.getName(), b.getName());
    assertEquals(a.getNum(), b.getNum());
    assertArrayEquals(a.getGeneric(), b.getGeneric());
  }

  private List<SomeObject<int[]>> createResponseList(int numElements) {
    return IntStream.range(0, numElements)
      .mapToObj(i -> SomeObject.create(i, "array has " + i + "elements",
        IntStream.range(0, numElements).toArray()))
      .collect(Collectors.toList());
  }

  /**
   * Dummy operation that {@link TestUtilities#getResponseType(Class, String)} can use reflection on
   * to construct a Type corresponding to a "Flux<SomeObject<int[]>>".
   */
  @SuppressWarnings("unused")
  private Flux<SomeObject<int[]>> declareReturnType() {
    return Flux.empty();
  }
}