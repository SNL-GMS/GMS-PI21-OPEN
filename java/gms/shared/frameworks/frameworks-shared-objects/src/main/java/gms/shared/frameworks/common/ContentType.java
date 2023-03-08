package gms.shared.frameworks.common;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a small set of Content-Type HTTP headers used by GMS.
 */
public enum ContentType {

  JSON(ContentType.JSON_NAME),
  JSON_STREAM(ContentType.JSON_STREAM_NAME),
  MSGPACK(ContentType.MSGPACK_NAME),
  MSGPACK_STREAM(ContentType.MSGPACK_STREAM_NAME);

  public static final String JSON_NAME = "application/json";
  public static final String JSON_STREAM_NAME = "application/stream+json";
  public static final String MSGPACK_NAME = "application/msgpack";
  public static final String MSGPACK_STREAM_NAME = "application/stream+msgpack";

  private final String type;

  ContentType(String type) {
    this.type = type;
  }

  /**
   * @return default {@link ContentType} for non-streaming content
   */
  public static ContentType defaultContentType() {
    return JSON;
  }

  /**
   * @return default {@link ContentType} for streaming content
   */
  public static ContentType defaultStreamingContentType() {
    return JSON_STREAM;
  }

  /**
   * Constructs a {@link ContentType} from the provided string. The string should correspond to
   * standard formats used for HTTP Content-Type or Accept-Type headers, e.g. "application/json".
   *
   * @param contentType string defining a content type, not null
   * @return {@link ContentType} corresponding to the provided contentType string
   * @throws NullPointerException if the contentType string is null
   * @throws IllegalArgumentException if the contentType string does not correspond to any of the
   * {@link ContentType} literals.
   */
  public static ContentType parse(String contentType) {
    Objects.requireNonNull(contentType, "parse requires non-null contentType");

    switch (contentType) {
      case JSON_NAME:
        return JSON;
      case JSON_STREAM_NAME:
        return JSON_STREAM;
      case MSGPACK_NAME:
        return MSGPACK;
      case MSGPACK_STREAM_NAME:
        return MSGPACK_STREAM;
      default:
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
  }

  /**
   * Determines whether the provided {@link ContentType} is a streaming type.
   *
   * @param contentType {@link ContentType}, not null
   * @return true if the {@link ContentType} is a streaming type
   * @throws NullPointerException if the {@link ContentType} is null
   */
  public static boolean isStreaming(ContentType contentType) {
    Objects.requireNonNull(contentType, "isStreaming requires non-null contentType");
    return Set.of(ContentType.JSON_STREAM, ContentType.MSGPACK_STREAM).contains(contentType);
  }

  @Override
  public String toString() {
    return type;
  }
}
