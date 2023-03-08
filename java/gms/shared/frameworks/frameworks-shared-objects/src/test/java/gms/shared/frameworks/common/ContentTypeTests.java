package gms.shared.frameworks.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentTypeTests {

  @Test
  void testDefaultTypes() {
    assertAll(
      () -> assertEquals(ContentType.JSON, ContentType.defaultContentType()),
      () -> assertEquals(ContentType.JSON_STREAM, ContentType.defaultStreamingContentType())
    );
  }

  @Test
  void testParse() {
    assertAll(
      () -> assertEquals(ContentType.JSON, ContentType.parse(ContentType.JSON_NAME)),
      () -> assertEquals(ContentType.JSON_STREAM,
        ContentType.parse(ContentType.JSON_STREAM_NAME)),
      () -> assertEquals(ContentType.MSGPACK, ContentType.parse(ContentType.MSGPACK_NAME)),
      () -> assertEquals(ContentType.MSGPACK_STREAM,
        ContentType.parse(ContentType.MSGPACK_STREAM_NAME))
    );
  }

  @Test
  void testParseValidatesInput() {
    assertTrue(assertThrows(NullPointerException.class, () -> ContentType.parse(null))
      .getMessage().contains("parse requires non-null contentType"));
  }

  @Test
  void testParseInvalidInputThrows() {
    assertTrue(assertThrows(IllegalArgumentException.class,
      () -> ContentType.parse("not a valid type"))
      .getMessage().contains("Unknown content type: "));
  }

  @ParameterizedTest
  @EnumSource(value = ContentType.class)
  void testIsStreaming(ContentType contentType) {
    assertEquals(contentType.toString().startsWith("application/stream+"),
      ContentType.isStreaming(contentType));
  }

  @Test
  void testIsStreamingValidatesParameters() {
    assertTrue(assertThrows(NullPointerException.class, () -> ContentType.isStreaming(null))
      .getMessage().contains("isStreaming requires non-null contentType"));
  }
}