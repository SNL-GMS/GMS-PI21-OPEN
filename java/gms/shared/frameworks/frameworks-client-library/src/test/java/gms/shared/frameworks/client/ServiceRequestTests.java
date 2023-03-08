package gms.shared.frameworks.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceRequestTests {

  private URL url;
  private static final Object body = 123;
  private static final JavaType responseType = new ObjectMapper().constructType(String.class);
  private static final Duration timeout = Duration.ofSeconds(123);
  private static final ContentType requestFormat = ContentType.JSON;
  private static final ContentType responseFormat = ContentType.MSGPACK;

  @BeforeEach
  void init() throws MalformedURLException {
    url = new URL("http:/foo.com/bar");
  }

  @Test
  void testFrom() {
    assertRequestAsExpected(ServiceRequest.from(
        url, body, timeout, responseType, requestFormat, responseFormat),
      requestFormat, responseFormat);
  }

  @Test
  void testNegativeTimeoutThrowsException() {
    Duration duration = Duration.ofMillis(-1);
    ServiceRequest.Builder srBuilder = ServiceRequest.builder(
      url, body, duration, responseType);

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
      () -> srBuilder.build());
    assertEquals("Timeout cannot be negative", e.getMessage());
  }

  @Test
  void testBuilderDefaultsFormats() {
    assertRequestHasDefaultFormats(ServiceRequest.builder(
      url, body, timeout, responseType).build());
  }

  private void assertRequestAsExpected(ServiceRequest req,
    ContentType requestFormat, ContentType responseFormat) {
    assertNotNull(req);
    assertEquals(url, req.getUrl());
    assertEquals(body, req.getBody());
    assertEquals(timeout, req.getTimeout());
    assertEquals(responseType, req.getResponseType());
    assertEquals(requestFormat, req.getRequestFormat());
    assertEquals(responseFormat, req.getResponseFormat());
  }

  private void assertRequestHasDefaultFormats(ServiceRequest req) {
    final ContentType defaultFormat = ContentType.defaultContentType();
    assertRequestAsExpected(req, defaultFormat, defaultFormat);
  }
}
