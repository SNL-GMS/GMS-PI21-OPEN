package gms.shared.frameworks.service;

import gms.shared.frameworks.common.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestTests {
  private static final Request jsonRequest = new RequestJsonAcceptAndContentTypes();
  private static final Request jsonStreamRequest = new RequestJsonStreamAcceptAndContentTypes();
  private static final Request messagePackRequest = new RequestMessagePackAcceptAndContentTypes();
  private static final Request messagePackStreamRequest = new RequestMessagePackStreamAcceptAndContentTypes();
  private static final Request unknownRequest = new RequestUnknownAcceptAndContentTypes();

  @Test
  void testClientAcceptsMsgpack() {
    assertTrue(messagePackRequest.clientAcceptsMsgpack());
    assertTrue(messagePackStreamRequest.clientAcceptsMsgpack());
    assertFalse(jsonRequest.clientAcceptsMsgpack());
    assertFalse(jsonStreamRequest.clientAcceptsMsgpack());
    assertFalse(unknownRequest.clientAcceptsMsgpack());
  }

  @Test
  void testClientSentMsgpack() {
    assertTrue(messagePackRequest.clientSentMsgpack());
    assertTrue(messagePackStreamRequest.clientSentMsgpack());
    assertFalse(jsonRequest.clientSentMsgpack());
    assertFalse(jsonStreamRequest.clientSentMsgpack());
    assertFalse(unknownRequest.clientSentMsgpack());
  }

  private static class RequestJsonAcceptAndContentTypes extends RequestImplementation {
    @Override
    public Optional<String> getHeader(String name) {
      if ("Accept".equals(name) || "Content-Type".equals(name)) {
        return Optional.of(ContentType.JSON_NAME);
      }

      return Optional.empty();
    }
  }

  private static class RequestJsonStreamAcceptAndContentTypes extends RequestImplementation {
    @Override
    public Optional<String> getHeader(String name) {
      if ("Accept".equals(name) || "Content-Type".equals(name)) {
        return Optional.of(ContentType.JSON_STREAM_NAME);
      }

      return Optional.empty();
    }
  }

  private static class RequestMessagePackAcceptAndContentTypes extends RequestImplementation {
    @Override
    public Optional<String> getHeader(String name) {
      if ("Accept".equals(name) || "Content-Type".equals(name)) {
        return Optional.of(ContentType.MSGPACK_NAME);
      }

      return Optional.empty();
    }
  }

  private static class RequestMessagePackStreamAcceptAndContentTypes extends RequestImplementation {
    @Override
    public Optional<String> getHeader(String name) {
      if ("Accept".equals(name) || "Content-Type".equals(name)) {
        return Optional.of(ContentType.MSGPACK_STREAM_NAME);
      }

      return Optional.empty();
    }
  }

  private static class RequestUnknownAcceptAndContentTypes extends RequestImplementation {
    @Override
    public Optional<String> getHeader(String name) {
      if ("Accept".equals(name) || "Content-Type".equals(name)) {
        return Optional.of("unknown");
      }

      return Optional.empty();
    }
  }

  /**
   * Dummy implementation of {@link Request} used to test the default operation implementations.
   */
  private static class RequestImplementation implements Request {

    @Override
    public Optional<String> getPathParam(String name) {
      return Optional.empty();
    }

    @Override
    public String getBody() {
      return null;
    }

    @Override
    public byte[] getRawBody() {
      return new byte[0];
    }

    @Override
    public Optional<String> getHeader(String name) {
      return Optional.empty();
    }

    @Override
    public Map<String, String> getHeaders() {
      return null;
    }
  }
}