package gms.shared.frameworks.client;

import java.net.http.HttpRequest.BodyPublisher;
import java.util.function.Function;


/**
 * A protocol for encoding HTTP request body content.
 * W is the type of the data on the wire, e.g. String or byte[].
 *
 * @param <W> the type on the wire, e.g. String or byte[]
 */
interface RequestContentProtocol<W> {

  /**
   * A function from W to a JDK HTTP BodyPublisher.
   * This is typically implemented with a method reference on the BodyPublishers class,
   * e.g. BodyPublisher::ofString;
   */
  Function<W, BodyPublisher> bodyEncoder();

  /**
   * Serializes an object into W.
   * This is typically implemented by using a Jackson ObjectMapper.
   *
   * @param data the data to serialize
   * @return an instance of W
   * @throws Exception if serialization fails, etc.
   */
  W serialize(Object data) throws Exception;
}
