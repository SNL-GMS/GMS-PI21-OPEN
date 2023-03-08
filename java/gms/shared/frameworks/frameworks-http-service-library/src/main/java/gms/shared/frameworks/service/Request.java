package gms.shared.frameworks.service;

import gms.shared.frameworks.common.ContentType;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for an HTTP request.  You can retrieve things from it such as it's body or query
 * params.  Intended to be implemented by using an underlying HTTP library.
 */
public interface Request {

  /**
   * Gets a path parameter, which is one that is part of the URL e.g. /person/john, where 'john' is
   * the path parameter
   *
   * @param name the name of the parameter
   * @return optional value of the parameter with the specified name
   */
  Optional<String> getPathParam(String name);

  /**
   * Gets the body as a String
   *
   * @return the body
   */
  String getBody();

  /**
   * Gets the body of the request as a raw byte array to be used in e.g. messagepack
   * deserialization
   *
   * @return the raw bytes of the body of the request
   */
  byte[] getRawBody();

  /**
   * Gets the header with the specified name
   *
   * @param name the name of the header
   * @return optional value of the header with the specified name
   */
  Optional<String> getHeader(String name);

  /**
   * Gets all headers
   *
   * @return a map from the name of the header to the value of the header
   */
  Map<String, String> getHeaders();

  /**
   * Determines if the request specified msgpack response
   *
   * @return true if the request specified msgpack is allowable, false otherwise
   */
  default boolean clientAcceptsMsgpack() {
    return isHeaderMessagePackContentType(this, "Accept");
  }

  /**
   * Determines if the request sent message pack format
   *
   * @return true if the request header (Content-Type) indicates msgpack, false otherwise
   */
  default boolean clientSentMsgpack() {
    return isHeaderMessagePackContentType(this, "Content-Type");
  }

  /**
   * Determines if the provided header from the provided {@link Request} represents a MessagePack
   * {@link ContentType}.
   *
   * @param request {@link Request}
   * @param header header attribute to read
   * @return true if the header's value corresponds to a MessagePack ContentType, false in any other
   * case (header does not exist, header does not correspond to a ContentType, header is a
   * non-MessagePack ContentType)
   */
  private static boolean isHeaderMessagePackContentType(Request request, String header) {
    return parseContentTypeFromHeader(request, header)
      .filter(c -> ContentType.MSGPACK.equals(c) || ContentType.MSGPACK_STREAM.equals(c))
      .isPresent();
  }

  /**
   * Get the provided header from the provided {@link Request} and parse it into a {@link
   * ContentType}.
   *
   * @param request {@link Request}
   * @param header header attribute to read
   * @return Optional ContentType parsed from the header. The Optional is empty if the Request does
   * not have the header or if a ConentType can not be parsed from the header value.
   */
  private static Optional<ContentType> parseContentTypeFromHeader(Request request, String header) {
    try {
      return request.getHeader(header).map(ContentType::parse);
    } catch (Exception ex) {
      return Optional.empty();
    }
  }
}
